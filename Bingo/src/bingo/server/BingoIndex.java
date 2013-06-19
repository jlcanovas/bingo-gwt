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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * This is an auxiliary class to keep stored the indexes used by the application
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
@PersistenceCapable
public class BingoIndex {
	public static final String BINGO_INDEX_ID = "bingoIndexID";
	public static final int MAX_BINGOS = 50;
	
	@PrimaryKey
	private String id;
	
	/**
	 * Known users. Format [userId, status]
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private HashMap<String, Integer> generatedUsers;
	
	/**
	 * Created games. Format [gameId, gameName]
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private HashMap<String, String> games;
	
	/**
	 * Map between existent users and bingos
	 */
	@Persistent(serialized="true", defaultFetchGroup = "true")
	private HashMap<String, String> user2Bingo;
	
	public BingoIndex(String id) {
		this.id = id;
		this.generatedUsers = new HashMap<String, Integer>();
		this.games = new HashMap<String, String>();
		this.user2Bingo = new HashMap<String, String>();
	}

	public boolean existUser(String userId) {
		return generatedUsers.get(userId) != null;
	}
	
	public void grantStatusToUser(String userId, Integer status) {
		synchronized(this) {
			// Workaround to fix a problem with JDO. I do not why but if I do not
			// access to user2Bingo BEFORE putting anything into generatedUsers, 
			// user2Bingo will be emptied
			user2Bingo.size(); 
			
			generatedUsers.put(userId, status);
		} 
	}
	
	public Integer getUserStatus(String userId) {
		return generatedUsers.get(userId);
	}
	
	public String getGameForUser(String userId) {
		return user2Bingo.get(userId);
	}
	
	public void assignGameToUser(String userId, String gameId) {
		synchronized(this) {
			user2Bingo.put(userId, gameId);
		}
	}
	
	public void registerGame(String gameId, String gameName) {
		synchronized(this) {
			games.put(gameId, gameName);
		}
	}
	
	public int getTotalGames() {
		return games.keySet().size();
	}
	
	public String[][] getGames() {
		String[][] result = null;
		if(games.keySet().size() > 0) {
			Iterator<String> gameIds = games.keySet().iterator();
			result = new String[games.keySet().size()][2];
			int i = 0;
			while(gameIds.hasNext()) {
				String gameId = gameIds.next();
				String gameName = games.get(gameId);
				result[i++] = new String[] { gameName, gameId};
			}
		}
		return result;
	}
		
	public void printState() {
		System.out.print("Games size: " + games.size());
		System.out.print(" -- GeneratedUsers size: " + generatedUsers.size());
		System.out.print(" -- User2Bingo size: " + user2Bingo.size() + "\n");
	}

	public void cleanGame(String owner, String gameId, List<String> usersToExpel, int newStatus) {
		synchronized(this) {
			games.size(); // As before, if have to "use" games to fetch the data
			games.remove(gameId);
			for(String userId : usersToExpel) {
				generatedUsers.put(userId, newStatus);
				user2Bingo.remove(userId);
			}
			generatedUsers.put(owner, newStatus);
			user2Bingo.remove(owner);
		}
		
	}

	
}
