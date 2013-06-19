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

package bingo.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * The client side stub for bingo RPC service
 *
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
@RemoteServiceRelativePath("bingoService")
public interface BingoService extends RemoteService {
	// GENERAL SERVICES
	/**
	 * Creates a new unique user id. Changes the status of the user
	 * from NO_LOGGED to LOGGED
	 * @return The unique id of the user
	 */
	String getUserId();
	
	final static int NO_LOGGED = 0;
	final static int LOGGED = 1;
	final static int ADMIN = 2;
	final static int PARTICIPANT = 3;
	/**
	 * Returns the status of the user id according to the previous values
	 * @param userId
	 * @return The status of the user according to the previous values
	 * @throws IllegalArgumentException
	 */
	int statusUserId(String userId) throws IllegalArgumentException;
	
	final static int RUNNING = 0;
	final static int FINISHED = 1;
	final static int TERMINATED = 2;
	/**
	 * Retursn the status of the bingo of a user according to the previous values
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	int statusBingo(String userId) throws IllegalArgumentException;
	
	/**
	 * An user joins a Bingo
	 * @param userId The user id joining the bingo
	 * @param gameId The game id to join
	 * @param password The password of the game
	 * @throws IllegalArgumentException 
	 */
	void joinBingo(String userId, String gameId, String password) throws IllegalArgumentException;
	
	/**
	 * Array of existing Bingos
	 * Format: [ [nameBingo1 , idBingo1], [nameBingo2 , idBingo2], ...]
	 * @return
	 * @throws IllegalArgumentException
	 */
	String[][] getBingos() throws IllegalArgumentException;	
	
	// ADMIN BEHAVIOR
	/**
	 * Creates a new bingo game
	 * @param owner
	 * @param name
	 * @param password
	 * @return
	 * @throws IllegalArgumentException
	 */
	String createBingo(String owner, String name, String password) throws IllegalArgumentException;
	
	/**
	 * Finish a existing Bingo
	 * @param owner
	 * @throws IllegalArgumentException
	 */
	void finishBingo(String owner) throws IllegalArgumentException;
	
	/**
	 * Terminates an existing Bingo
	 * @param owner
	 */
	void terminateBingo(String owner) throws IllegalArgumentException;
	
	/**
	 * Gets the total votes of the bingo managed by the user
	 * or null if the bingoGame no longer exists
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	int[][] getVotes(String userId) throws IllegalArgumentException;
	
	/**
	 * Get the number of participants of the bingo managed by the user
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	int getTotalParticipants(String userId) throws IllegalArgumentException;
	
	// PARTICIPANT BEHAVIOR
	/**
	 * Counts the vote for a user, returns a prime-based number describing the lines made
	 * or -1 if the bingoGame is closed or no longer exists
	 * @param userId
	 * @param row
	 * @param col
	 * @return
	 * @throws IllegalArgumentException
	 */
	long voteCell(String userId, int row, int col) throws IllegalArgumentException;
	
	/**
	 * Gets the status votes of a particular bingo game of a user
	 * or null if the bingoGame is closed or no longer exists
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	boolean[][] statusBingoVotes(String userId) throws IllegalArgumentException;

	/**
	 * Gets the status lines of a particular bingo game of a user according to the prime-number format
	 * or -1 if the bingoGame is closed or no longer exists
	 * @param userId
	 * @return
	 * @throws IllegalArgumentException
	 */
	long statusBingoLines(String userId) throws IllegalArgumentException;
}
