package org.orange.parser.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CourseTest {
	private static final int TEACHER_NUMBER = 4;
	private static final int TIME_AND_ADDRESS_NUMBER = 5;

	private Course sample;

	@Before
	public void setClass() {
		sample = new Course();
		for(int i = 1 ; i <= TEACHER_NUMBER ; i++)
			sample.addTeachers("教师 " + i);
		for(int i = 1 ; i <= TIME_AND_ADDRESS_NUMBER ; i++)
			sample.addTimeAndAddress(new Course.TimeAndAddress());
	}

	@Test
	public void testClone() throws CloneNotSupportedException {
		Course clone = sample.clone();
		clone.getTeachers().clear();
		clone.getTimeAndAddress().clear();
		Assert.assertTrue(clone.getTeachers().isEmpty());
		Assert.assertTrue(clone.getTimeAndAddress().isEmpty());
		Assert.assertEquals(TEACHER_NUMBER, sample.getTeachers().size());
		Assert.assertEquals(TIME_AND_ADDRESS_NUMBER, sample.getTimeAndAddress().size());
	}

	@Test(expected = CloneNotSupportedException.class)
	public void testSubclassClone() throws CloneNotSupportedException {
		Course subclass = new Course() {};
		subclass.clone();
	}
}
