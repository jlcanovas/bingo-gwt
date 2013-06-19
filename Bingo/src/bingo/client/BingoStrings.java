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

import java.util.Map;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Static internationalization
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */ 
public interface BingoStrings extends Constants {
	@Meaning("Title of the bingo page")
	String title();
	
	@Meaning("String of the grid cells")
	Map<String, String> map();
	 
	@Meaning("Error message when the user cannot get and id for a game")
	String errorUserCreation();
	
	@Meaning("Error message when the server is unreachable to vote")
	String errorUserVote();
	
	@Meaning("Info message to show in the landing page to create a Bingo name")
	String createBingoName();
	
	@Meaning("Info message to show in the landing page to create a Bingo password")
	String createBingoPassword();
	
	@Meaning("Info message to show in the landing page to join to a Bingo Name")
	String joinToBingoName();
	
	@Meaning("Info message to show in the landing page to join to a Bingo Password")
	String joinToBingoPassword();
	
	@Meaning("Info message to show in the landing page when there are no bingos to join in")
	String noBingos();

	@Meaning("Info message to show when the game is created")
	String currentGame();

	@Meaning("Message to show when no item is selected")
	String noSelectedItem();

	@Meaning("Welcoming message when a bingo is created")
	String welcomeMessageCreation();

	@Meaning("Welcoming message when joining a bingo")
	String welcomeMessageJoin();

	@Meaning("Info message to indicate that the cell was already voted")
	String alreadyVoted();

	@Meaning("Admin button text to finish a Bingo")
	String finishBingo();

	@Meaning("Info message to show once the bingo is finished")
	String finishedBingo();

	@Meaning("Info message to show when there is a synchronizatio problem in the participants")
	String errorUserSynchronization();

	@Meaning("Admin button text to terminate a Bingo")
	String terminateButton();

	@Meaning("Info message to show once the bingo is terminated")
	String terminatedBingo();

	@Meaning("Translation for warning message title")
	String warning();

	@Meaning("Translation for cancel word")
	String cancel();

	@Meaning("Translation for accept word")
	String accept();

	@Meaning("Info message to warn the user that the bingo will be terminated")
	String terminateMessage();

	@Meaning("Translation for Info message title")
	String info();

	@Meaning("Info message to inform the user that the bingo has been finished")
	String finishMessage();

	@Meaning("Translation for Download Bingo message title")
	String download();

	@Meaning("Message to show in the main page when a new user arrives")
	String welcomeMessage();

	@Meaning("Title for left panel to create")
	String leftPanelTitle();

	@Meaning("Title for left panel to join")
	String rightPanelTitle();
}
