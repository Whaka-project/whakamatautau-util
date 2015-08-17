package org.whaka.util.reflection.comparison;

import com.google.common.base.MoreObjects;

public class TestEntities {

	public static class Person {

		private String name;
		private int age;
		private boolean male;

		public Person(String name, int age, boolean male) {
			
			this.name = name;
			this.age = age;
			this.male = male;
		}
		
		public String getName() {
			return name;
		}
		
		public int getAge() {
			return age;
		}
		
		public boolean isMale() {
			return male;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.addValue(getName())
					.addValue(getAge())
					.addValue(isMale() ? "male" : "female")
					.toString();
		}
	}
	
	public static class JobPosition {

		private String title;
		private Person employee;

		public JobPosition(String title, Person employee) {
			this.title = title;
			this.employee = employee;
		}
		
		public String getTitle() {
			return title;
		}
		
		public Person getEmployee() {
			return employee;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.addValue(getTitle())
					.addValue(getEmployee())
					.toString();
		}
	}

	public static class Methods {
		
		public CharSequence publicGetCS() {
			return "";
		}
		
		public String publicGetString() {
			return "";
		}
		
		protected String protectedGetString() {
			return "";
		}
		
		String defaultGetString() {
			return "";
		}
		
		@SuppressWarnings({ "static-method", "unused" })
		private String privateGetString() {
			return "";
		}
	}
}
