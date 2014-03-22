package com.mavedev.battery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {
	
	@Test
	public void itReturnsCorrectValueWhenTimeIsExactOneHour(){
		assertEquals("1 Hrs, 0 minutes", Utils.getHoursAndMinutes(60*60));
	}
	
	@Test
	public void itReturnsCorrectValueWhenTimeIsGreaterThanOneHour(){
		assertEquals("1 Hrs, 3 minutes", Utils.getHoursAndMinutes(63*60));
	}
	
	@Test
	public void itReturnsCorrectValueWhenTimeIsLessThanOneHour(){
		assertEquals("59 minutes", Utils.getHoursAndMinutes(59*60));
	}
	
	@Test
	public void itReturnsCorrectValueWhenTimeIsMoreThanTwoHour(){
		assertEquals("2 Hrs, 5 minutes", Utils.getHoursAndMinutes(125*60));
	}
}
