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

import java.util.List;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import bingo.client.BingoGrid;
import bingo.client.BingoService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server implementation for bing service
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
@SuppressWarnings("serial")
public class BingoServiceImpl extends RemoteServiceServlet implements BingoService {
	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public BingoServiceImpl() {
		super();
		// Accessing index and checking if it exists
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			Query q = pm.newQuery(BingoIndex.class);
			List<BingoIndex> indexes = (List<BingoIndex>) q.execute();
			if(indexes.size() == 0) {
				BingoIndex index = new BingoIndex(BingoIndex.BINGO_INDEX_ID);
				pm.makePersistent(index);
			}
		} finally {
			pm.close();
		}
	}

	@Override
	public long voteCell(String userId, int row, int col) {
		if(row < 0 || row > BingoGrid.ROW || col < 0 || col > BingoGrid.COL) 
			throw new IllegalArgumentException("Index of cell row/col out of bounds");
		if(userId == null || userId.equals("")) 
			throw new IllegalArgumentException("The userId is not valid");

		String gameId = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);

			if(!index.existUser(userId))
				throw new IllegalArgumentException("The userId is not valid");

			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null)
			throw new IllegalArgumentException("The userId is not valid");

		long value = 0;

		// Updating the Bingo from JDO
		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			game.userVote(userId, row, col);
			value = game.userCheckVotes(userId);
		} finally {
			pm.close();
		}

		return value;
	}

	@Override
	public String getUserId()  {
		String userId = UUID.randomUUID().toString();

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			index.grantStatusToUser(userId, new Integer(BingoService.LOGGED));
		} finally {
			pm.close();
		}

		return userId;
	}

	@Override
	public String createBingo(String owner, String name, String password) throws IllegalArgumentException {
		if(name == null || name.equals("")) 
			throw new IllegalArgumentException("The name cannot be empty");
		if(password == null || password.equals("")) 
			throw new IllegalArgumentException("The password cannot be empty");

		String id = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);

			if(index.getTotalGames() < BingoIndex.MAX_BINGOS) {
				BingoGame game = new BingoGame(owner, name, password);
				id = game.getId();

				index.registerGame(id, name);
				index.assignGameToUser(owner, id);
				index.grantStatusToUser(owner, new Integer(BingoService.ADMIN));

				pm.makePersistent(game);
			} else {
				throw new IllegalArgumentException("Too many Bingos running and I don't have money to pay a heavy cloud infrastructure, sorry");
			}

		} finally {
			pm.close();
		}

		return id;
	}

	@Override
	public String[][] getBingos() throws IllegalArgumentException {
		String[][] result = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			result = index.getGames();
		} finally {
			pm.close();
		}

		return result;
	}

	@Override
	public void joinBingo(String userId, String gameId, String password) throws IllegalArgumentException {
		if(password == null || password.equals("")) 
			throw new IllegalArgumentException("The password is not valid");

		if(userId == null || userId.equals("")) 
			throw new IllegalArgumentException("The userId is not valid");

		if(gameId == null || gameId.equals(""))  
			throw new IllegalArgumentException("The gameId is not valid");

		// Updating the Bingo from JDO
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			if(!index.existUser(userId))
				throw new IllegalArgumentException("The userId is not valid");

			// Incredible, I have to do this to force JDO to bring the data
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);
			//			for (String user : game.getUsersPlaying()) 
			//				game.getTotalVotes(user);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			if(!game.checkPassword(password)) 
				throw new IllegalArgumentException("The password is not valid");

			game.userJoin(userId);
			index.assignGameToUser(userId, gameId);
			index.grantStatusToUser(userId, new Integer(BingoService.PARTICIPANT));
		} finally {
			pm.close();
		}
	}

	@Override
	public int[][] getVotes(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals(""))  
			throw new IllegalArgumentException("The userId is not valid");

		int[][] totalVotes = null;

		String gameId = null;
		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null) 
			throw new IllegalArgumentException("The userId is not valid");

		// Reading the Bingo from JDO
		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			totalVotes = game.getTotalVotes(userId);
		} finally {
			pm.close();
		}

		return totalVotes;
	}

	@Override
	public int statusUserId(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals("")) 
			return BingoService.NO_LOGGED;

		Integer result = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			result = index.getUserStatus(userId);
		} finally {
			pm.close();
		}

		if(result == null) 
			return BingoService.NO_LOGGED;
		return result.intValue();
	}

	@Override
	public int statusBingo(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals(""))  
			throw new IllegalArgumentException("The userId is not valid");

		int result = BingoService.TERMINATED;

		String gameId = null;
		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null)
			throw new IllegalArgumentException("The userId is not in a game");

		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			if(game.isActive()) 
				result = BingoService.RUNNING;
			else
				result = BingoService.FINISHED;
		} finally {
			pm.close();
		}

		return result;
	}

	@Override
	public long statusBingoLines(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals(""))  
			throw new IllegalArgumentException("The userId is not valid");

		long result = 0;
		String gameId = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null)
			throw new IllegalArgumentException("The userId is not in a game");

		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			result = game.userCheckVotes(userId);
		} finally {
			pm.close();
		}

		return result;
	}

	@Override
	public boolean[][] statusBingoVotes(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals(""))  
			throw new IllegalArgumentException("The userId is not valid");

		boolean[][] result = new boolean[5][5];
		String gameId = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null)
			throw new IllegalArgumentException("The userId is not valid");

		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			result = game.userIndividualVotes(userId);
		} finally {
			pm.close();
		}

		return result;
	}

	@Override
	public void finishBingo(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals("")) 
			throw new IllegalArgumentException("The userId is not valid");

		String gameId = null;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);

			if(gameId == null)
				throw new IllegalArgumentException("The userId is not valid");

			BingoGame game = pm.getObjectById(BingoGame.class, gameId);
			game.finishGame(userId);
		} finally {
			pm.close();
		}
	}

	@Override
	public void terminateBingo(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals("")) 
			throw new IllegalArgumentException("The userId is not valid");

		String gameId = null;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);

			if(gameId == null)
				throw new IllegalArgumentException("The userId is not valid");

			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			// If the game is still active, it is finished
			if(game.isActive())
				game.finishGame(userId);

			List<String> usersToExpel = game.getUsersPlaying();
			index.cleanGame(userId, game.getId(), usersToExpel, BingoService.LOGGED);

			pm.deletePersistent(game);
		} finally {
			pm.close();
		}

	}

	@Override
	public int getTotalParticipants(String userId) throws IllegalArgumentException {
		if(userId == null || userId.equals(""))  
			throw new IllegalArgumentException("The userId is not valid");

		int totalParticipants = 0;

		String gameId = null;

		// Accessing index
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			BingoIndex index = pm.getObjectById(BingoIndex.class, BingoIndex.BINGO_INDEX_ID);
			gameId = index.getGameForUser(userId);
		} finally {
			pm.close();
		}

		if(gameId == null) 
			throw new IllegalArgumentException("The userId is not valid");

		// Reading the Bingo from JDO
		pm = pmfInstance.getPersistenceManager();
		try {
			BingoGame game = pm.getObjectById(BingoGame.class, gameId);

			if(game == null)  
				throw new IllegalArgumentException("The gameId is not valid");

			totalParticipants = game.getUsersPlaying().size();
		} finally {
			pm.close();
		}

		return totalParticipants;
	}




}
