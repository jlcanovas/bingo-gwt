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

import bingo.client.resources.BingoResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * This class encapsulates the behavior for the Bingo grid. Similarly to 
 * BingoCell class, it is simple but allows me to manage bingos in a 
 * easier way.
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
public class BingoGrid extends Grid {
	/**
	 * Prime numbers are used by the service to generate a prime-composed number with which
	 * I can color the lines that have been achieved
	 */
	final static int[] PRIME_NUMBERS = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67 };
	
	private static final String CELL_HEIGHT = "90px";
	private static final String CELL_WIDTH = "90px";
	
	public static final int ROW = 3;
	public static final int COL = 6;

	final BingoStrings strings = (BingoStrings) GWT.create(BingoStrings.class);
	
	BingoCell cells[][];

	public BingoGrid() {
		super(ROW, COL);
		initGrid();
	}
	

	private void initGrid() {
		cells = new BingoCell[ROW][COL];
		
		this.setCellPadding(5);

		String cellKey = "";
		for (int row = 0; row < ROW; ++row) 
			for (int col = 0; col < COL; ++col) {
				cellKey = "cell" + row + col;
				
				// Getting the picture
				ImageResource imageResource = getImageResource(row, col);
								
				// Getting the text
				String cellString = strings.map().get(cellKey);
				
				BingoCell bingoCell = new BingoCell(imageResource.getSafeUri(), cellString, ""); 
				
				// Building the panel
				this.setWidget(row, col, bingoCell);
				cells[row][col] = bingoCell;
				
				this.getCellFormatter().setStyleName(row, col, "cell-noselected");
				this.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
				this.getCellFormatter().setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_MIDDLE);
				this.getCellFormatter().setWidth(row, col, CELL_WIDTH);
				this.getCellFormatter().setHeight(row, col, CELL_HEIGHT);
			}
	}
	
	/**
	 * Sets the string to show the number of votes casted for a cell.
	 * 
	 * @param row Row 
	 * @param col Column
	 * @param voteString The String to show the total votes casted
	 * @param totalString The String to show the participants
	 */
	public void setVoteString(int row, int col, String voteString, String totalString) {
		if(row < 0 || row > ROW || col < 0 || col > COL) 
			throw new IllegalArgumentException("Row or Coll out of bounds");
		if(voteString == null)
			throw new IllegalArgumentException("The vote string cannot be null");
		
		BingoCell bingoCell = cells[row][col];
		bingoCell.setVotes(voteString, totalString);
	}
	
	/**
	 * Votes for a cel
	 * 
	 * @param row Row
	 * @param col Column
	 */
	public void voteForCell(int row, int col) {
		if(row < 0 || row > ROW || col < 0 || col > COL) 
			throw new IllegalArgumentException("Row or Coll out of bounds");

		BingoCell bingoCell = cells[row][col];
		bingoCell.setVoted(true);
		
		// Updating cell style
		this.getCellFormatter().setStyleName(row, col, "cell-selected");
	}
	
	/**
	 * Votes against a cell
	 *  
	 * @param row Row
	 * @param col Column
	 */
	public void voteAgainstCell(int row, int col) {
		if(row < 0 || row > ROW || col < 0 || col > COL) 
			throw new IllegalArgumentException("Row or Coll out of bounds");

		BingoCell bingoCell = cells[row][col];
		bingoCell.setVoted(false);
		
		// Updating cell style
		this.getCellFormatter().setStyleName(row, col, "cell-noselected");
	}
	
	/**
	 * Checks if a cell has already been voted
	 * 
	 * @param row Row
	 * @param col Column
	 * @return Boolean telling if the cell was voted or not
	 */
	public boolean cellHasBeenVoted(int row, int col) {
		if(row < 0 || row > ROW || col < 0 || col > COL) 
			throw new IllegalArgumentException("Row or Coll out of bounds");
		
		BingoCell bingoCell = cells[row][col];
		return bingoCell.isVoted();
	}
	
	/**
	 * Color the lines (vertical and horizontal) achieved by the participant. 
	 * 
	 * @param result Prime-composed number 
	 */
	public void colorLines(Long result) {
		final long value = result.longValue();
		if(value != 0) { 
			// Checking possible bingo-line rows
			for(int col = 0; col < COL; col++) {
				if(value % PRIME_NUMBERS[col] == 0) {
					for(int row = 0; row < ROW; row++) {
						this.getCellFormatter().setStyleName(row, col, "cell-line");
					}
				}
			}

			// Checking possible bingo-line cols
			for(int row = 0; row < ROW; row++) {
				if(value % PRIME_NUMBERS[BingoGrid.COL + row] == 0) {
					for(int col = 0; col < COL; col++) {
						this.getCellFormatter().setStyleName(row, col, "cell-line");
					}
				}
			}
		}
	}
	
	/**
	 * Color those cells that have already been voted
	 * 
	 * @param result
	 */
	public void colorVotes(boolean[][] result) {
		for(int row = 0; row < ROW; row++) 
			for(int col = 0; col < COL; col++) 
				if(result[row][col])
					this.getCellFormatter().setStyleName(row, col, "cell-selected");
	}
	
	/**
	 * I have to do this because reflection is not allowed in GWT client part
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private ImageResource getImageResource(int row, int col) {
		if(row < 0 || row > ROW || col < 0 || col > COL) 
			throw new IllegalArgumentException("Row or Coll out of bounds");
		
		if(row == 0 && col == 0) 
			return BingoResources.INSTANCE.cell00();
		else if(row == 0 && col == 1) 
			return BingoResources.INSTANCE.cell01();
		else if(row == 0 && col == 2) 
			return BingoResources.INSTANCE.cell02();
		else if(row == 0 && col == 3) 
			return BingoResources.INSTANCE.cell03();
		else if(row == 0 && col == 4) 
			return BingoResources.INSTANCE.cell04();
		else if(row == 0 && col == 5) 
			return BingoResources.INSTANCE.cell05();
		else if(row == 1 && col == 0) 
			return BingoResources.INSTANCE.cell10();
		else if(row == 1 && col == 1) 
			return BingoResources.INSTANCE.cell11();
		else if(row == 1 && col == 2) 
			return BingoResources.INSTANCE.cell12();
		else if(row == 1 && col == 3) 
			return BingoResources.INSTANCE.cell13();
		else if(row == 1 && col == 4) 
			return BingoResources.INSTANCE.cell14();
		else if(row == 1 && col == 5) 
			return BingoResources.INSTANCE.cell15();
		else if(row == 2 && col == 0) 
			return BingoResources.INSTANCE.cell20();
		else if(row == 2 && col == 1) 
			return BingoResources.INSTANCE.cell21();
		else if(row == 2 && col == 2) 
			return BingoResources.INSTANCE.cell22();
		else if(row == 2 && col == 3) 
			return BingoResources.INSTANCE.cell23();
		else if(row == 2 && col == 4) 
			return BingoResources.INSTANCE.cell24();
		else if(row == 2 && col == 5) 
			return BingoResources.INSTANCE.cell25();
		else  
			return BingoResources.INSTANCE.cell25();
	}
	
}
