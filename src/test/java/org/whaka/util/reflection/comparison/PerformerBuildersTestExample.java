package org.whaka.util.reflection.comparison;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import org.whaka.util.reflection.UberClasses;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder;
import org.whaka.util.reflection.comparison.performers.AbstractComparisonPerformer;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * Class provides test examples of using "comparison" package manually.
 * Just examples nothing more.
 */
@SuppressWarnings("unused")
public class PerformerBuildersTestExample {

	public static void main(String[] args) {
		
		Child c1 = new ChildImpl(17, "qw", true);
		Child c2 = new ChildImpl(12, "qwe", false);
		
		Parent p1 = new ParentImpl(42L, "pop", 36, Arrays.asList(c1, c2), new int[]{1,2,3});
		Parent p2 = new ParentImpl(42L, "pop", 36, Arrays.asList(c1, c2), new int[]{1,2,3});
		
		ComparisonResult result = PARENT_COMPARISON_3.qwerty123456qwerty654321(p1, p2);
		printResult(result);
	}
	
	public static void printResult(ComparisonResult result) {
		printResult(null, result, 0);
	}
	
	private static void printResult(ClassPropertyKey key, ComparisonResult result, int level) {
		for (int i = 0; i < level; i++)
			System.out.print("	");
		if (key != null)
			System.out.print(key + ": ");
		System.out.println(result);
		if (result instanceof ComplexComparisonResult) {
			Map<ClassPropertyKey, ComparisonResult> props =
					((ComplexComparisonResult) result).getPropertyResults();
			for (Map.Entry<ClassPropertyKey, ComparisonResult> e : props.entrySet())
				printResult(e.getKey(), e.getValue(), level + 1);
		}
	}
	
	public static interface Child {

		long getId();
		
		String getName();
		
		boolean isBoy();
	}
	
	public static class ChildImpl implements Child {

		private long id;
		private String name;
		private boolean boy;
		
		public ChildImpl(long id, String name, boolean boy) {
			this.id = id;
			this.name = name;
			this.boy = boy;
		}

		@Override
		public long getId() {
			return id;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public boolean isBoy() {
			return boy;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("id", getId())
					.add("name", getName())
					.add("boy", isBoy())
					.toString();
		}
	}
	
	public static interface Parent {

		long getId();
		
		String getName();
		
		int getAge();
		
		List<Child> getChild();
		
		int[] getArr();
	}
	
	public static class ParentImpl implements Parent {

		private int[] arr;
		private long id;
		private String name;
		private int age;
		private List<Child> child;
		
		public ParentImpl(long id, String name, int age, List<Child> child, int[] arr) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.child = child;
			this.arr = arr;
		}

		@Override
		public long getId() {
			return id;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public int getAge() {
			return age;
		}
		
		@Override
		public List<Child> getChild() {
			return child;
		}
		
		@Override
		public int[] getArr() {
			return arr;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("id", getId())
					.add("name", getName())
					.add("age", getAge())
					.add("arr", getArr())
					.add("child", getChild())
					.toString();
		}
	}
	
	private static ComparisonPerformer<Child> CHILD_COMPARISON =
			new AbstractComparisonPerformer<PerformerBuildersTestExample.Child>("ChildComparison") {
				
				@Override
				public ComparisonResult qwerty123456qwerty654321(Child actual, Child expected) {
					return new ComplexComparisonResultBuilder<>(Child.class)
							.compare("getId()", actual.getId(), expected.getId())
							.compare("getName()", actual.getName(), expected.getName())
							.compare("isBoy()", actual.isBoy(), expected.isBoy())
							.build(actual, expected, this);
				}
			};
	
	private static ComparisonPerformer<Parent> PARENT_COMPARISON =
			new AbstractComparisonPerformer<PerformerBuildersTestExample.Parent>("ParentComparison") {
		
				@Override
				public ComparisonResult qwerty123456qwerty654321(Parent actual, Parent expected) {
					return new ComplexComparisonResultBuilder<>(Parent.class)
							.compare("getId()", actual.getId(), expected.getId())
							.compare("getName()", actual.getName(), expected.getName())
							.compare("getAge()", actual.getAge(), expected.getAge())
							.compare("getArr()", actual.getArr(), expected.getArr())
							.compare("getChild()", actual.getChild(), expected.getChild(), ComparisonPerformers.list(CHILD_COMPARISON))
							.build(actual, expected, this);
				}
			};
	
	private static ComparisonPerformer<Child> CHILD_COMPARISON_2 =
			ComparisonPerformers.buildProperties(Child.class)
				.addProperty("getId()", Child::getId)
				.addProperty("getName()", Child::getName)
				.addProperty("isBoy()", Child::isBoy)
				.build("ChildComparison2");
	
	private static ComparisonPerformer<Parent> PARENT_COMPARISON_2 =
			ComparisonPerformers.buildProperties(Parent.class)
				.addProperty("getId()", Parent::getId)
				.addProperty("getName()", Parent::getName)
				.addProperty("getAge()", Parent::getAge)
				.addProperty("getArr()", Parent::getArr)
				.addProperty("getChild()", Parent::getChild)
				.configureDynamicPerformer(p -> p.registerDelegate(Child.class, CHILD_COMPARISON_2))
				.build("ParentComparison2");
	
	private static ComparisonPerformer<Child> CHILD_COMPARISON_3 =
			ComparisonPerformers.buildGetters(Child.class)
				.build("ChildComparison3");

	private static ComparisonPerformer<Parent> PARENT_COMPARISON_3 =
			ComparisonPerformers.buildGetters(Parent.class)
				.configureDynamicPerformer(p -> {
					p.setDefaultDelegate(ComparisonPerformers.REFLECTIVE_EQUALS);
					p.registerDelegate(Child.class, CHILD_COMPARISON_3);
					p.registerCollectionDelegateProvider(UberClasses.cast(List.class), Child.class, ComparisonPerformers::list);
				})
				.build("ParentComparison3");
}