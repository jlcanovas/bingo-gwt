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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The client side stub for bingo RPC service async
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
public interface BingoServiceAsync {
	void voteCell(String userId, int row, int col, AsyncCallback<Long> callback);
	void getBingos(AsyncCallback<String[][]> callback);
	void createBingo(String owner, String name, String password, AsyncCallback<String> callback);
	void getUserId(AsyncCallback<String> callback);
	void joinBingo(String userId, String gameId, String password, AsyncCallback<Void> callback);
	void getVotes(String userId, AsyncCallback<int[][]> callback);
	void statusUserId(String userId, AsyncCallback<Integer> callback);
	void statusBingoLines(String userId, AsyncCallback<Long> callback);
	void statusBingoVotes(String userId, AsyncCallback<boolean[][]> callback);
	void finishBingo(String owner, AsyncCallback<Void> callback);
	void terminateBingo(String owner, AsyncCallback<Void> callback);
	void statusBingo(String userId, AsyncCallback<Integer> callback);
	void getTotalParticipants(String userId, AsyncCallback<Integer> callback);
}
