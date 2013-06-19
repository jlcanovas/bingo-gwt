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

import java.util.Date;

import bingo.client.resources.BingoResources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Main entry point class: the landing page.
 */
public class Bingo implements EntryPoint {
	// Cookie identifier
	public final static String COOKIE_ID = "bingo-bad-presentation";
	// Set the value of the hours mesaure of the expiration date of the cookie
	public final static int EXPIRATION_VALUE = 2;

	// Timer used to refresh in admin view
	public final static int ADMIN_TIMER = 1000;
	// Timer used to refresh in user view
	public final static int USER_TIMER = 1000;

	/**
	 * Keeps the userId (managed by cookies)
	 */
	String userId = null; 

	/**
	 * Support for i18n
	 */
	final BingoStrings strings = (BingoStrings) GWT.create(BingoStrings.class);
	
	/**
	 * Easy way to show messages to the user
	 */
	final HTML message = new HTML("Loading...");

	/**
	 * Create a remote service proxy to talk to the server-side Bingo service.
	 */
	private final BingoServiceAsync bingoService = GWT.create(BingoService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Label titleLabel = new Label();
		titleLabel.setText(strings.title());
		RootPanel.get("title").add(titleLabel);

		// This is the general notification text box
		RootPanel.get("message").add(message);

		// Cheking if the user has already an user id
		final String cookie = Cookies.getCookie(COOKIE_ID);

		// Validating the cookie
		bingoService.statusUserId(cookie, new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				message.setText(caught.getMessage());
				Cookies.removeCookie(COOKIE_ID);
			}
			@Override
			public void onSuccess(Integer status) {
				// TODO: Control the logged status (I should not get a user if s/he is already logged)
				if(status.intValue() == BingoService.NO_LOGGED || status.intValue() == BingoService.LOGGED) {
					bingoService.getUserId(new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							message.setText(strings.errorUserCreation());
						}

						@Override
						public void onSuccess(String result) {
							userId = result;
						}
					});

					message.setHTML("");
					
					// Showing image to enter
					ImageResource imageResource = BingoResources.INSTANCE.entry();
					Image entryImage = new Image(imageResource.getSafeUri());
					RootPanel.get("buttons").add(entryImage);
					
					// Selecting behavior (admin / voter) 
					HorizontalPanel entryPoint = new HorizontalPanel();
					
					entryPoint.setStyleName("mainSelectorPanel");
					entryPoint.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
					entryPoint.setSpacing(50);

					// 
					// CREATING A BINGO
					//
					VerticalPanel leftPanel = new VerticalPanel();
					leftPanel.setStyleName("selectorPanel");
					
					Label leftPanelTitle = new Label();
					leftPanelTitle.setStyleName("selectorPanelTitle");
					leftPanelTitle.setText(strings.leftPanelTitle());
					leftPanel.add(leftPanelTitle);
					leftPanel.setCellHorizontalAlignment(leftPanelTitle, HasHorizontalAlignment.ALIGN_CENTER);
					
					Grid leftSubPanel = new Grid(2,2);
					leftSubPanel.setWidget(0, 0, new Label(strings.createBingoName()));
					final TextBox bingoNameBox = new TextBox();
					leftSubPanel.setWidget(0, 1, bingoNameBox);

					leftSubPanel.setWidget(1, 0, new Label(strings.createBingoPassword()));
					final PasswordTextBox bingoPasswordBox = new PasswordTextBox();
					leftSubPanel.setWidget(1, 1, bingoPasswordBox);  
					leftPanel.add(leftSubPanel);

					final Button createButton = new Button("Create");
					createButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							bingoService.createBingo(userId, bingoNameBox.getText(), bingoPasswordBox.getText(), new AsyncCallback<String>() {
								@Override
								public void onFailure(Throwable caught) {
									message.setText(caught.getMessage());
								}
								@Override
								public void onSuccess(final String gameId) {
									final BingoGrid bingoGrid = new BingoGrid();
									initAdminGrid(userId, bingoGrid, false);
									RootPanel.get("bingoTable").clear();
									RootPanel.get("bingoTable").add(bingoGrid);									
									message.setText(strings.welcomeMessageCreation());

									// Setting the cookie
									Date expirationDate = new Date();
									expirationDate.setHours(expirationDate.getHours() + EXPIRATION_VALUE);
									Cookies.setCookie(COOKIE_ID, userId, expirationDate);
								}
							});
						}
					});
					leftPanel.add(createButton);
					leftPanel.setCellHorizontalAlignment(createButton, HasHorizontalAlignment.ALIGN_CENTER);
					entryPoint.add(leftPanel);
					

					// 
					// JOINING
					//
					VerticalPanel rightPanel = new VerticalPanel();
					rightPanel.setStyleName("selectorPanel");

					Label rightPanelTitle = new Label();
					rightPanelTitle.setStyleName("selectorPanelTitle");
					rightPanelTitle.setText(strings.rightPanelTitle());
					rightPanel.add(rightPanelTitle);
					rightPanel.setCellHorizontalAlignment(rightPanelTitle, HasHorizontalAlignment.ALIGN_CENTER);
					
					Grid rightSubPanel = new Grid(2,2);
					rightSubPanel.setWidget(0, 0, new Label(strings.joinToBingoName()));
					final ListBox joinExistingBingoBox = new ListBox();
					bingoService.getBingos(new AsyncCallback<String[][]>() {
						@Override
						public void onFailure(Throwable caught) {
							joinExistingBingoBox.addItem(strings.noBingos());
						}
						@Override
						public void onSuccess(String[][] result) {
							if(result == null) {
								joinExistingBingoBox.addItem(strings.noBingos());
							} else{
								for(int i = 0; i < result.length; i++) {
									String gameName = result[i][0];
									String gameId = result[i][1];
									joinExistingBingoBox.addItem(gameName, gameId);
								}
							}
						}
					});
					rightSubPanel.setWidget(0, 1, joinExistingBingoBox);	

					rightSubPanel.setWidget(1, 0, new Label(strings.joinToBingoPassword()));
					final PasswordTextBox joinBingoPasswordBox = new PasswordTextBox();
					rightSubPanel.setWidget(1, 1, joinBingoPasswordBox);	
					rightPanel.add(rightSubPanel);
					
					final Button joinButton = new Button("Join");
					joinButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							int index = joinExistingBingoBox.getSelectedIndex();
							if(index < 0) 
								message.setText(strings.noSelectedItem());
							else {
								final String gameId = joinExistingBingoBox.getValue(index);
								if(gameId == null) 
									message.setText(strings.noSelectedItem());
								else {
									bingoService.joinBingo(userId, gameId, joinBingoPasswordBox.getText(), new AsyncCallback<Void>(){
										@Override
										public void onFailure(Throwable caught) {
											message.setText(caught.getMessage());
										}

										@Override
										public void onSuccess(Void result) {
											final BingoGrid bingoGrid = new BingoGrid();
											initUserGrid(bingoGrid);
											RootPanel.get("bingoTable").clear();
											RootPanel.get("bingoTable").add(bingoGrid);
											
											message.setText(strings.welcomeMessageJoin());

											// Setting the cookie
											Date expirationDate = new Date();
											expirationDate.setHours(expirationDate.getHours() + EXPIRATION_VALUE);
											Cookies.setCookie(COOKIE_ID, userId, expirationDate);
										}});
								}
							}
						}
					});
					rightPanel.add(joinButton);
					rightPanel.setCellHorizontalAlignment(joinButton, HasHorizontalAlignment.ALIGN_CENTER);
					entryPoint.add(rightPanel);

					RootPanel.get("bingoTable").add(entryPoint);

				} else if(status.intValue() == BingoService.ADMIN) {
					message.setText("Detected cookie: " + cookie +  " as admin");
					userId = cookie;

					bingoService.statusBingo(userId, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							message.setText(caught.getMessage());
						}
						@Override
						public void onSuccess(Integer result) {
							int status = result.intValue();
							final BingoGrid bingoGrid = new BingoGrid();
							if(status == BingoService.RUNNING) {
								initAdminGrid(userId, bingoGrid, false);
								message.setText(strings.welcomeMessageCreation());
							} else if (status == BingoService.FINISHED) {
								initAdminGrid(userId, bingoGrid, true);
								message.setText(strings.finishMessage());
							}
							RootPanel.get("bingoTable").clear();
							RootPanel.get("bingoTable").add(bingoGrid);
						}
					});

				} else if(status.intValue() == BingoService.PARTICIPANT) {
					message.setText("Detected cookie: " + cookie +  " as participant");
					userId = cookie;

					final BingoGrid bingoGrid = new BingoGrid();
					initUserGrid(bingoGrid);
					updateUserGrid(bingoGrid, null);
					RootPanel.get("bingoTable").clear();
					RootPanel.get("bingoTable").add(bingoGrid);
				}
			}
		});
	}

	private void initAdminGrid(final String userId, final BingoGrid bingoGrid, final boolean hasFinished) {
		final Timer timer = new Timer() {
			int totalParticipants = 0;
			
			@Override
			public void run() {
				bingoService.getTotalParticipants(userId, new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Integer result) {
						totalParticipants = result.intValue();
					}
				});
				
				bingoService.getVotes(userId, new AsyncCallback<int[][]>() {
					@Override
					public void onFailure(Throwable caught) {
						message.setText(caught.getMessage());
					}
					@Override
					public void onSuccess(int[][] result) {
						if(result == null) {
							result = new int[BingoGrid.ROW][BingoGrid.COL];
							for(int i = 0; i < BingoGrid.ROW; i++) 
								for(int j = 0; j < BingoGrid.COL; j++)
									result[i][j] = 0;
						}

						for (int row = 0; row < BingoGrid.ROW; ++row) 
							for (int col = 0; col < BingoGrid.COL; ++col) {
								bingoGrid.setVoteString(row, col, String.valueOf(result[row][col]), String.valueOf(totalParticipants));
							}
					}
				});
			}
		};

		// If the game is not finished, repeat; if so, run once
		if(!hasFinished) 
			timer.scheduleRepeating(ADMIN_TIMER);
		else
			timer.run();

		HorizontalPanel panel = new HorizontalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final Button finishButton = new Button();

		// If the game is not finished, allow finishing it; if so, allow download data
		if(!hasFinished) 
			finishButton.setText(strings.finishBingo());
		else
			finishButton.setText(strings.download());

		finishButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!hasFinished)
					bingoService.finishBingo(userId, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							message.setText(caught.getMessage());
						}
						@Override
						public void onSuccess(Void result) {
							message.setText(strings.finishedBingo());
							finishButton.setText(strings.download());
							timer.cancel();
						}
					});

				bingoService.getVotes(userId, new AsyncCallback<int[][]>() {
					@Override
					public void onFailure(Throwable caught) {
						message.setText(caught.getMessage());
					}
					@Override
					public void onSuccess(int[][] result) {
						// Create a popup dialog box
						final DialogBox box = new DialogBox();
						box.setText(strings.info());
						box.setAnimationEnabled(true);

						final Button boxAcceptButton = new Button(strings.accept());
						boxAcceptButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								box.hide();
							}
						});

						Label boxLabel = new Label();
						boxLabel.setText(strings.finishMessage());
						HTML voteData = new HTML();
						if(result == null) {
							result = new int[BingoGrid.ROW][BingoGrid.COL];
							for(int i = 0; i < BingoGrid.ROW; i++) 
								for(int j = 0; j < BingoGrid.COL; j++)
									result[i][j] = 0;
						}

						String cellKey = "";
						String cellString = "";
						String voteDataString = "";
						for (int row = 0; row < BingoGrid.ROW; ++row) 
							for (int col = 0; col < BingoGrid.COL; ++col) {
								cellKey = "cell" + row + col;
								cellString = strings.map().get(cellKey);
								voteDataString += "&nbsp;&nbsp;&nbsp;" + cellString + " = " + result[row][col] + "<br>";
							}
						voteData.setHTML(voteDataString);
						voteData.addStyleName("boxData");

						VerticalPanel boxPanel = new VerticalPanel();
						boxPanel.addStyleName("boxPanel");
						boxPanel.add(boxLabel);
						boxPanel.add(voteData);

						HorizontalPanel buttonsPanel = new HorizontalPanel();
						buttonsPanel.add(boxAcceptButton);
						boxPanel.add(buttonsPanel);
						boxPanel.setCellHorizontalAlignment(buttonsPanel, HasHorizontalAlignment.ALIGN_CENTER);
						

						box.setWidget(boxPanel);
						box.center();
					}
				});
			}
		});
		panel.add(finishButton);

		final Button terminateButton = new Button(strings.terminateButton());
		terminateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Create a popup dialog box
				final DialogBox box = new DialogBox();
				box.setText(strings.warning());
				box.setAnimationEnabled(true);

				final Button boxCancelButton = new Button(strings.cancel());
				boxCancelButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						box.hide();
					}
				});
				final Button boxAcceptButton = new Button(strings.accept());
				boxAcceptButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						bingoService.terminateBingo(userId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								message.setText(caught.getMessage());
							}

							@Override
							public void onSuccess(Void result) {
								message.setText(strings.terminatedBingo());
								timer.cancel();
							}

						});
						box.hide();
					}
				});

				final Label boxLabel = new Label();
				boxLabel.setText(strings.terminateMessage());

				VerticalPanel boxPanel = new VerticalPanel();

				boxPanel.addStyleName("boxPanel");
				boxPanel.add(boxLabel);

				HorizontalPanel buttonsPanel = new HorizontalPanel();
				buttonsPanel.add(boxCancelButton);
				buttonsPanel.add(boxAcceptButton);
				boxPanel.add(buttonsPanel);

				box.setWidget(boxPanel);
				box.center();
			}
		});
		panel.add(terminateButton);

		RootPanel.get("buttons").clear();
		RootPanel.get("buttons").add(panel); 
	}

	private void initUserGrid(final BingoGrid bingoGrid) {
		bingoGrid.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cell cell = bingoGrid.getCellForEvent(event);
				if(cell != null) {
					final int row = cell.getRowIndex();
					final int col = cell.getCellIndex();
					if(!bingoGrid.cellHasBeenVoted(row, col)) {
						bingoGrid.voteForCell(row, col);

						bingoService.voteCell(userId, row, col, new AsyncCallback<Long>() {
							@Override
							public void onFailure(Throwable caught) {
								message.setText(strings.errorUserVote());
								bingoGrid.voteAgainstCell(row, col);
							}
							@Override
							public void onSuccess(Long result) {
								// For now, no control of lines completed
//								bingoGrid.colorLines(result);
							}
						});
					} else {
						message.setText(strings.alreadyVoted());
					}
				}
			}
		});

		Timer timer = new Timer() {
			@Override
			public void run() {
				updateUserGrid(bingoGrid, this);
			}
		};

		timer.scheduleRepeating(USER_TIMER);
		
		RootPanel.get("buttons").clear();
	}


	private void updateUserGrid(final BingoGrid bingoGrid, final Timer timer) {
		bingoService.statusBingoVotes(userId, new AsyncCallback<boolean[][]>() {
			@Override
			public void onFailure(Throwable caught) {
				message.setText(strings.errorUserSynchronization());
				if(timer != null) timer.cancel();
			}
			@Override
			public void onSuccess(boolean[][] result) {
				bingoGrid.colorVotes(result);
			}
		});

		// For now, no control of lines completed
//		bingoService.statusBingoLines(userId, new AsyncCallback<Long>() {
//			@Override
//			public void onFailure(Throwable caught) {
//				message.setText(strings.errorUserSynchronization());
//				if(timer != null) timer.cancel();
//			}
//			@Override
//			public void onSuccess(Long result) {
//				bingoGrid.colorLines(result);
//			}
//		});
	}

	

	

}
