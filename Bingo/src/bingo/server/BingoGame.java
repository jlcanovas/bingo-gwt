/*******************************************************************************
 *  This file is part of Bad Presentation Bingo.
 *
 *  Bad Presentation Bingo is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License verion 3
 *  as published by the Free Software Foundation
 *
 *  Bad Presentation Bingo is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *    Javier Canovas (http://jlcanovas.es) 
 *******************************************************************************/

package bingo.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import bingo.client.BingoGrid;

/**
 * This class represents a particular bingo game at the server side
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
@PersistenceCapable
public class BingoGame {
	/**
	 * Prime numbers are used to calculate the vertical/horizontal lines achieved by the user
	 */
	final static long[] PRIME_NUMBERS = new long[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67 };
	
	/**
	 * Algorithm to be used to hash the password
	 */
	final static String HASH_ALGORITHM = "SHA-256";

	/**
	 * The id unique for this game
	 */
	@PrimaryKey
	private String id;
	
	/**
	 * The id of the user who is owner of the game 
	 */
	@Persistent
	private String owner;
	
	/**
	 * Name of the game
	 */
	@Persistent
	private String name;
	
	/**
	 * Password to access to the game
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private byte[] password;
	
	/**
	 * The individual vote of each participant 
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private Map<String, boolean[][]> userVotes;
	
	/**
	 * The counting of the votes
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private int[][] totalVotes;
	
	/**
	 * Indicates if the bingo is active (playable)
	 */
	@Persistent
	private boolean active;

	public BingoGame(String userId, String name, String password) {
		if(name == null || name.equals("")) 
			throw new IllegalArgumentException("The name cannot be empty");
		if(password == null || password.equals("")) 
			throw new IllegalArgumentException("The password cannot be empty");

		this.owner = userId;
		this.name = name;
		this.active = true;
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			md.update(password.getBytes("UTF-8"));
			byte[] digest = md.digest();
			this.password = digest;
		} catch (NoSuchAlgorithmException e) {
			this.password = password.getBytes(); 
		} catch (UnsupportedEncodingException e) {
			this.password = password.getBytes(); 
		}
		this.id = UUID.randomUUID().toString();
		this.userVotes = new HashMap<String, boolean[][]>();
		this.totalVotes = new int[BingoGrid.ROW][BingoGrid.COL];
		for(int i = 0; i < BingoGrid.ROW; i++) 
			for(int j = 0; j < BingoGrid.COL; j++)
				totalVotes[i][j] = 0;
	}

	
	public boolean isActive() {
		return active;
	}
	public void finishGame(String userId) {
		if(owner.equals(userId)) {
			active = false;
		} else {
			throw new IllegalArgumentException("The userId is invalid");
		}
	}

	public String getOwner() {
		return owner;
	}
	
	public List<String> getUsersPlaying() {
		List<String> usersPlaying = new ArrayList<String>();
		
		Iterator<String> userIds = userVotes.keySet().iterator();
		while(userIds.hasNext()) {
			String userId = userIds.next();
			usersPlaying.add(userId);
		}
		
		return usersPlaying;
	}
	
	public boolean checkPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			md.update(password.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return Arrays.equals(this.password, digest);
		} catch (NoSuchAlgorithmException e) {
			return Arrays.equals(this.password, password.getBytes());
		} catch (UnsupportedEncodingException e) {
			return Arrays.equals(this.password, password.getBytes());
		}
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public int[][] getTotalVotes(String userId) {
		if(userId == null) // || !userId.equals(owner))
			throw new IllegalArgumentException("The user is not valid");

		int[][] result = new int[BingoGrid.ROW][BingoGrid.COL];
		for(int i = 0; i < BingoGrid.ROW; i++) 
			for(int j = 0; j < BingoGrid.COL; j++)
				result[i][j] = totalVotes[i][j];
		return result;
	}

	public void userJoin(String userId) {
		if(!this.active) 
			throw new IllegalStateException("The game is not playable");
		userCreateVotes(userId);
	}

	/**
	 * Initializes the votes of a user
	 * 
	 * @param userId
	 */
	private void userCreateVotes(String userId) {
		if(!this.active) 
			throw new IllegalStateException("The game is not playable");
		
		boolean[][] votes = new boolean[BingoGrid.ROW][BingoGrid.COL];
		for(int col = 0; col < BingoGrid.COL; col++) {
			for(int row = 0; row < BingoGrid.ROW; row++) {
				votes[row][col] = false;
			}
		}
		userVotes.put(userId, votes);
	}

	/**
	 * An user votes for a row/column. The user must exist
	 * 
	 * @param userId Id of the user
	 * @param row 0 < row < ROW
	 * @param col 0 < col < COL
	 */
	public void userVote(String userId, int row, int col) {
		if(!this.active) 
			throw new IllegalStateException("The game is not playable");
		if(row < 0 || row > BingoGrid.ROW || col < 0 || col > BingoGrid.COL) 
			throw new IllegalArgumentException("Index of row/col out of bounds");

		boolean[][] votes = userVotes.get(userId);
		if(votes == null) 
			throw new IllegalArgumentException("The user does not exist");

		if(votes[row][col] == false) {
			votes[row][col] = true;
			synchronized(this) {
				totalVotes[row][col] += 1;
			}
		}
	}


	/**
	 * Checks if the votes of the user includes bingo lines. The user must exist. 
	 * 
	 * The return format is a prime number with indicates which columns/rows include bingo lines.
	 * 
	 *      2  3  5  7  11
	 * 13 --+--+--+--+--+--
	 * 17 --+--+--+--+--+--
	 * 19 --+--+--+--+--+--
	 * 23 --+--+--+--+--+--
	 * 29 --+--+--+--+--+--
	 * 
	 * Example: if the first row and first column containes bingo lines, the return value will be 
	 * 13*2=26
	 * 
	 * @param userId Id of the user
	 * @return The prime-based number, 0 if there is no line 
	 */
	public long userCheckVotes(String userId) {
		if(!this.active) 
			throw new IllegalStateException("The game is not playable");
		
		boolean[][] votes = userVotes.get(userId);
		if(votes == null) 
			throw new IllegalArgumentException("The user is not playing this bingo");

		long result = 1;
		boolean atLeastOne = false;

		for(int col = 0; col < BingoGrid.COL; col++) {
			for(int row = 0; row < BingoGrid.ROW; row++) {
				if(votes[row][col] == false) break;
				if(row == (BingoGrid.ROW - 1)) {
					result *= PRIME_NUMBERS[col];
					atLeastOne = true;
				}
			}
		}

		for(int row = 0; row < BingoGrid.ROW; row++) {
			for(int col = 0; col < BingoGrid.COL; col++) {
				if(votes[row][col] == false) break;
				if(col == (BingoGrid.COL - 1)) {
					result *= PRIME_NUMBERS[BingoGrid.COL+row];
					atLeastOne = true;
				}
			}
		}

		if(atLeastOne)
			return result;
		else return 0;
	}

	/**
	 * Returns the individual votes of a user (the votes per cell)
	 * 
	 * @param userId
	 * @return nxn array with the active cells
	 */
	public boolean[][] userIndividualVotes(String userId) {
		if(!this.active) 
			throw new IllegalStateException("The game is not playable");
		
		boolean[][] votes = userVotes.get(userId);
		if(votes == null) 
			throw new IllegalArgumentException("The user is not playing this bingo");
		
		boolean[][] result = new boolean[BingoGrid.ROW][BingoGrid.COL];
		for(int col = 0; col < BingoGrid.COL; col++) {
			for(int row = 0; row < BingoGrid.ROW; row++) {
				result[row][col] = votes[row][col];
			}
		}
		
		return result;
	}
	
}
