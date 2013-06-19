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

package bingo.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface BingoResources extends ClientBundle {
	public static final BingoResources INSTANCE = GWT.create(BingoResources.class);
	
	@Source("cell00.png")
	ImageResource cell00();

	@Source("cell01.png")
	ImageResource cell01();

	@Source("cell02.png")
	ImageResource cell02();

	@Source("cell03.png")
	ImageResource cell03();

	@Source("cell04.png")
	ImageResource cell04();
	
	@Source("cell05.png")
	ImageResource cell05();
	
	@Source("cell10.png")
	ImageResource cell10();

	@Source("cell11.png")
	ImageResource cell11();

	@Source("cell12.png")
	ImageResource cell12();

	@Source("cell13.png")
	ImageResource cell13();

	@Source("cell14.png")
	ImageResource cell14();
	
	@Source("cell15.png")
	ImageResource cell15();

	@Source("cell20.png")
	ImageResource cell20();

	@Source("cell21.png")
	ImageResource cell21();

	@Source("cell22.png")
	ImageResource cell22();

	@Source("cell23.png")
	ImageResource cell23();

	@Source("cell24.png")
	ImageResource cell24();
	
	@Source("cell25.png")
	ImageResource cell25();

	@Source("entry.png")
	ImageResource entry();
}
