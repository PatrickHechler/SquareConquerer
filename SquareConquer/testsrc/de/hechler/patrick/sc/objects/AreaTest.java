package de.hechler.patrick.sc.objects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AreaTest {
	
	private final static String AREA_SMILEY_10_10 = ""
			+ "...####..."
			+ ".########."
			+ "##..##..##"
			+ "##..##..##"
			+ "##########"
			+ "##########"
			+ "#..####..#"
			+ "##......##"
			+ ".###..###."
			+ "...####..."
			;
	private static Area AREA_SMILEY;
	
	private final static String AREA_FULL_10_10 = ""
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			;
	private static Area AREA_FULL;
			
	private final static String AREA_EMPTY_10_10 = ""
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			;
	private static Area AREA_EMPTY;
			
	private final static String AREA_LEFT_10_10 = ""
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			;
	private static Area AREA_LEFT;
			
	private final static String AREA_RIGHT_10_10 = ""
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			;
	private static Area AREA_RIGHT;
			
	private final static String AREA_TOP_10_10 = ""
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			;
	private static Area AREA_TOP;
			
	private final static String AREA_BOTTOM_10_10 = ""
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			+ "##########"
			;
	private static Area AREA_BOTTOM;
			
	private final static String AREA_TOPLEFT_10_10 = ""
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			;
	private static Area AREA_TOPLEFT;
			
	private final static String AREA_BOTTOMLEFT_10_10 = ""
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			+ "#####....."
			;
	private static Area AREA_BOTTOMLEFT;
	
	private final static String AREA_TOPRIGHT_10_10 = ""
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			;
	private static Area AREA_TOPRIGHT;
			
	private final static String AREA_BOTTOMRIGHT_10_10 = ""
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".........."
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			+ ".....#####"
			;
	private static Area AREA_BOTTOMRIGHT;
	
	private final static String AREA_BORDER_10_10 = ""
			+ "##########"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "#........#"
			+ "##########"
			;
	private static Area AREA_BORDER;
	
	private final static String AREA_INNER_10_10 = ""
			+ ".........."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".########."
			+ ".........."
			;
	private static Area AREA_INNER;
	

	@BeforeAll
	static void initTest() throws Exception {
		AREA_SMILEY = createAreaFromString(AREA_SMILEY_10_10, 0, 0, 10, 10);
		AREA_FULL = createAreaFromString(AREA_FULL_10_10, 0, 0, 10, 10);
		AREA_EMPTY = createAreaFromString(AREA_EMPTY_10_10, 0, 0, 10, 10);
		AREA_LEFT = createAreaFromString(AREA_LEFT_10_10, 0, 0, 10, 10);
		AREA_RIGHT = createAreaFromString(AREA_RIGHT_10_10, 0, 0, 10, 10);
		AREA_TOP = createAreaFromString(AREA_TOP_10_10, 0, 0, 10, 10);
		AREA_BOTTOM = createAreaFromString(AREA_BOTTOM_10_10, 0, 0, 10, 10);
		AREA_BOTTOMLEFT = createAreaFromString(AREA_BOTTOMLEFT_10_10, 0, 0, 10, 10);
		AREA_BOTTOMRIGHT = createAreaFromString(AREA_BOTTOMRIGHT_10_10, 0, 0, 10, 10);
		AREA_TOPLEFT = createAreaFromString(AREA_TOPLEFT_10_10, 0, 0, 10, 10);
		AREA_TOPRIGHT = createAreaFromString(AREA_TOPRIGHT_10_10, 0, 0, 10, 10);
		AREA_BORDER = createAreaFromString(AREA_BORDER_10_10, 0, 0, 10, 10);
		AREA_INNER = createAreaFromString(AREA_INNER_10_10, 0, 0, 10, 10);
	}

	private static Area createAreaFromString(String areaString, int startX, int startY, int width, int height) {
		if (areaString.length() != width*height) {
			throw new RuntimeException("invalid sting length ("+areaString.length()+") for "+width+"x"+height);
		}
		Area result = new Area(startX, startY, width, height);
		AbsoluteManipulablePosition pos = new AbsoluteManipulablePosition(0, 0);
		for (int y=0; y<height; y++) {
			pos.setY(y);
			for (int x=0; x<width; x++) {
				pos.setX(x);
				if (areaString.charAt(y*width+x) == '#') {
					result.add(pos);
				}
			}
		}
		return result;
	}

	@AfterAll
	static void exitTest() throws Exception {
		AREA_SMILEY = null;
		AREA_FULL = null;
		AREA_EMPTY = null;
		AREA_LEFT = null;
		AREA_RIGHT = null;
		AREA_TOP = null;
		AREA_BOTTOM = null;
		AREA_BOTTOMLEFT = null;
		AREA_BOTTOMRIGHT = null;
		AREA_TOPLEFT = null;
		AREA_TOPRIGHT = null;
		AREA_BORDER = null;
		AREA_INNER = null;
	}
	
	
	@Test
	void testSize() {
		assertEquals(100, AREA_FULL.size());
		assertEquals(0, AREA_EMPTY.size());
		assertEquals(50, AREA_LEFT.size());
		assertEquals(50, AREA_RIGHT.size());
		assertEquals(50, AREA_TOP.size());
		assertEquals(50, AREA_BOTTOM.size());
		assertEquals(25, AREA_BOTTOMLEFT.size());
		assertEquals(25, AREA_BOTTOMRIGHT.size());
		assertEquals(25, AREA_TOPLEFT.size());
		assertEquals(25, AREA_TOPRIGHT.size());
		assertEquals(36, AREA_BORDER.size());
		assertEquals(64, AREA_INNER.size());
		// handgezaehlt
		assertEquals(64, AREA_SMILEY.size());
	}

	@Test
	void testIsEmpty() {
		// empty area returns true
		assertTrue(AREA_EMPTY.isEmpty());
		
		// all others returns false
		assertFalse(AREA_FULL.isEmpty());
		assertFalse(AREA_LEFT.isEmpty());
		assertFalse(AREA_RIGHT.isEmpty());
		assertFalse(AREA_TOP.isEmpty());
		assertFalse(AREA_BOTTOM.isEmpty());
		assertFalse(AREA_BOTTOMLEFT.isEmpty());
		assertFalse(AREA_BOTTOMRIGHT.isEmpty());
		assertFalse(AREA_TOPLEFT.isEmpty());
		assertFalse(AREA_TOPRIGHT.isEmpty());
		assertFalse(AREA_BORDER.isEmpty());
		assertFalse(AREA_INNER.isEmpty());
		assertFalse(AREA_SMILEY.isEmpty());
	}


	@Test
	void testContainsObject() {
		assertTrue(AREA_FULL.contains(AREA_INNER));
		assertTrue(AREA_FULL.contains(AREA_BORDER));
		assertTrue(AREA_FULL.contains(AREA_EMPTY));
		assertTrue(AREA_FULL.contains(AREA_FULL));
		assertTrue(AREA_FULL.contains(AREA_TOP));
		assertTrue(AREA_FULL.contains(AREA_BOTTOM));
		assertTrue(AREA_FULL.contains(AREA_LEFT));
		assertTrue(AREA_FULL.contains(AREA_RIGHT));
		assertTrue(AREA_FULL.contains(AREA_SMILEY));

		assertFalse(AREA_EMPTY.contains(AREA_INNER));
		assertFalse(AREA_EMPTY.contains(AREA_BORDER));
		assertFalse(AREA_EMPTY.contains(AREA_FULL));
		assertFalse(AREA_EMPTY.contains(AREA_SMILEY));
		// empty area should contain empty area
		assertTrue(AREA_EMPTY.contains(AREA_EMPTY));

		assertTrue(AREA_TOP.contains(AREA_TOP));
		// AREA_TOP does not contain AREA_BOTTOM and contains() should return false.
		assertFalse(AREA_TOP.contains(AREA_BOTTOM));
		assertFalse(AREA_TOP.contains(AREA_LEFT));
		assertFalse(AREA_TOP.contains(AREA_RIGHT));

		assertTrue(AREA_TOP.contains(AREA_TOPLEFT));
		assertTrue(AREA_TOP.contains(AREA_TOPRIGHT));
		assertFalse(AREA_TOP.contains(AREA_BOTTOMLEFT));
		assertFalse(AREA_TOP.contains(AREA_BOTTOMRIGHT));

		assertFalse(AREA_BOTTOM.contains(AREA_TOPLEFT));
		assertFalse(AREA_BOTTOM.contains(AREA_TOPRIGHT));
		assertTrue(AREA_BOTTOM.contains(AREA_BOTTOMLEFT));
		assertTrue(AREA_BOTTOM.contains(AREA_BOTTOMRIGHT));

		assertTrue(AREA_LEFT.contains(AREA_TOPLEFT));
		assertFalse(AREA_LEFT.contains(AREA_TOPRIGHT));
		assertTrue(AREA_LEFT.contains(AREA_BOTTOMLEFT));
		assertFalse(AREA_LEFT.contains(AREA_BOTTOMRIGHT));

		assertFalse(AREA_RIGHT.contains(AREA_TOPLEFT));
		assertTrue(AREA_RIGHT.contains(AREA_TOPRIGHT));
		assertFalse(AREA_RIGHT.contains(AREA_BOTTOMLEFT));
		assertTrue(AREA_RIGHT.contains(AREA_BOTTOMRIGHT));

	}

	@Test
	void testContainsPosition() {
		checkAllPositions(AREA_SMILEY, AREA_SMILEY_10_10);
		checkAllPositions(AREA_FULL, AREA_FULL_10_10);
		checkAllPositions(AREA_EMPTY, AREA_EMPTY_10_10);
		checkAllPositions(AREA_LEFT, AREA_LEFT_10_10);
		checkAllPositions(AREA_RIGHT, AREA_RIGHT_10_10);
		checkAllPositions(AREA_TOP, AREA_TOP_10_10);
		checkAllPositions(AREA_BOTTOM, AREA_BOTTOM_10_10);
		checkAllPositions(AREA_BOTTOMLEFT, AREA_BOTTOMLEFT_10_10);
		checkAllPositions(AREA_BOTTOMRIGHT, AREA_BOTTOMRIGHT_10_10);
		checkAllPositions(AREA_TOPLEFT, AREA_TOPLEFT_10_10);
		checkAllPositions(AREA_TOPRIGHT, AREA_TOPRIGHT_10_10);
		checkAllPositions(AREA_BORDER, AREA_BORDER_10_10);
		checkAllPositions(AREA_INNER, AREA_INNER_10_10);
	}
	
	private void checkAllPositions(Area area, String areaString) {
		int width = area.getxCnt();
		int height = area.getyCnt();
		assertEquals(areaString.length(), width*height);
		AbsoluteManipulablePosition pos = new AbsoluteManipulablePosition(0, 0);
		for (int y=0; y<height; y++) {
			pos.setY(y);
			for (int x=0; x<width; x++) {
				pos.setX(x);
				boolean posIsSet = areaString.charAt(y*width+x) == '#';
				assertEquals(posIsSet, area.contains(pos));
			}
		}
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				
			}
		}
	}

	@Test
	void testEqualsArea() {
		assertEquals(AREA_SMILEY, createAreaFromString(AREA_SMILEY_10_10, 0, 0, 10, 10));
		assertEquals(AREA_FULL, createAreaFromString(AREA_FULL_10_10, 0, 0, 10, 10));
		assertEquals(AREA_EMPTY, createAreaFromString(AREA_EMPTY_10_10, 0, 0, 10, 10));
		assertEquals(AREA_LEFT, createAreaFromString(AREA_LEFT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_RIGHT, createAreaFromString(AREA_RIGHT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_TOP, createAreaFromString(AREA_TOP_10_10, 0, 0, 10, 10));
		assertEquals(AREA_BOTTOM , createAreaFromString(AREA_BOTTOM_10_10, 0, 0, 10, 10));
		assertEquals(AREA_BOTTOMLEFT, createAreaFromString(AREA_BOTTOMLEFT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_BOTTOMRIGHT, createAreaFromString(AREA_BOTTOMRIGHT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_TOPLEFT, createAreaFromString(AREA_TOPLEFT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_TOPRIGHT, createAreaFromString(AREA_TOPRIGHT_10_10, 0, 0, 10, 10));
		assertEquals(AREA_BORDER, createAreaFromString(AREA_BORDER_10_10, 0, 0, 10, 10));
		assertEquals(AREA_INNER, createAreaFromString(AREA_INNER_10_10, 0, 0, 10, 10));

		assertNotEquals(AREA_FULL, AREA_EMPTY);
		assertNotEquals(AREA_EMPTY, AREA_FULL);
		assertNotEquals(AREA_TOP, AREA_LEFT);
		assertNotEquals(AREA_INNER, AREA_BORDER);
		assertNotEquals(AREA_FULL, AREA_TOP);
		assertNotEquals(AREA_FULL, AREA_BOTTOM);
		assertNotEquals(AREA_FULL, AREA_LEFT);
		assertNotEquals(AREA_FULL, AREA_RIGHT);
		assertNotEquals(AREA_EMPTY, AREA_TOP);
		assertNotEquals(AREA_EMPTY, AREA_BOTTOM);
		assertNotEquals(AREA_EMPTY, AREA_LEFT);
		assertNotEquals(AREA_EMPTY, AREA_RIGHT);
		assertNotEquals(AREA_TOP, AREA_FULL);
		assertNotEquals(AREA_BOTTOM, AREA_FULL);
		assertNotEquals(AREA_LEFT, AREA_FULL);
		assertNotEquals(AREA_RIGHT, AREA_FULL);
		assertNotEquals(AREA_TOP, AREA_EMPTY);
		assertNotEquals(AREA_BOTTOM, AREA_EMPTY);
		assertNotEquals(AREA_LEFT, AREA_EMPTY);
		assertNotEquals(AREA_RIGHT, AREA_EMPTY);
		assertNotEquals(AREA_TOP, AREA_TOPLEFT);
		assertNotEquals(AREA_TOP, AREA_TOPRIGHT);
		assertNotEquals(AREA_TOPLEFT, AREA_TOP);
		assertNotEquals(AREA_TOPRIGHT, AREA_TOP);

	}

	
	
	@Test
	@Disabled
	void testIterator() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testToArray() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testToArrayTArray() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAdd() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRemoveObject() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testContainsAllCollectionOfQ() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAddAllCollectionOfQextendsPosition() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRetainAllCollectionOfQ() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRemoveAllCollectionOfQ() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testClear() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testArea() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testContainsAllArea() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAddAllArea() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAddAllPositionInt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRemovePosition() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRemoveAllArea() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRetainAllArea() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testNewCreateMoveDirection() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testClone() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRightangleIntIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testRightangle() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testGetxCnt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testGetyCnt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testEqualsRightangle() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testXMinYMax() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testXMaxYMin() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testXMaxYMax() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testClone1() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAbsoluteManipulablePositionIntInt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testAbsoluteManipulablePositionPosition() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testGetX() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testSetX() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testGetY() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testSetY() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testMove() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testNewCreateMoveDirection1() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testClone2() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testEqualsPosition() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testSetxCnt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testSetyCnt() {
		fail("Not yet implemented");
	}


}
