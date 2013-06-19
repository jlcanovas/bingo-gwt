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


import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * This class encapsulates the behavior of a Bingo cell. It's simple, but 
 * helps me to manage the cells easily
 * 
 * @author Javier Canovas (http://jlcanovas.es)
 *
 */
public class BingoCell extends Grid {
	private static final String IMAGE_HEIGHT = "90px";
	private static final String IMAGE_WIDTH = "90px";
	
	private Label votes;
	private boolean voted;
	
	public BingoCell(SafeUri uri, String label, String votes) {
		super(3,1);
		this.setCellPadding(3);
		
		FlowPanel imagePanel = new FlowPanel();
		imagePanel.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		Image cellImage = new Image(uri);
		cellImage.setStyleName("cell-image");
		imagePanel.add(cellImage);
		
		Label labelText = new Label();
		labelText.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		labelText.setText(label);
		labelText.setStyleName("cell-label");
		
		Label votesText = new Label(votes);
		this.votes = votesText;
		this.votes.setStyleName("cell-votes");
		
		this.voted = false;
		
		this.setWidget(0, 0, imagePanel);
		this.getCellFormatter().setHeight(0, 0, IMAGE_HEIGHT);
		this.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		this.setWidget(1, 0, labelText);
		this.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		this.setWidget(2, 0, this.votes);
		this.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
	}

	public String getVotes() {
		return votes.getText();
	}
	public void setVotes(String votes, String total) {
		this.remove(this.votes);
		this.votes = new HTML(votes + "/" + total);
		this.votes.setStyleName("cell-votes");
		this.setWidget(2, 0, this.votes);
	}

	public boolean isVoted() {
		return voted;
	}
	public void setVoted(boolean voted) {
		this.voted = voted;
	}
	
	
	
}
