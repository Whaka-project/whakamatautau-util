package org.whaka.util.asserts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.whaka.asserts.AssertError;
import org.whaka.asserts.ComparisonAssertResult;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.TestEntities.JobPosition;
import org.whaka.util.reflection.comparison.TestEntities.Person;
import org.whaka.util.reflection.comparison.performers.CompositeComparisonPerformer;

public class ComparisonAssertResultExample {

	public static void main(String[] args) {
		
		Person martin = new Person("Martin", 30, true);
		Person martina = new Person("Martina", 30, false);

		JobPosition racer = new JobPosition("F1 Racer", martin);
		JobPosition spy = new JobPosition("International Spy", martina);
		
		CompositeComparisonPerformer<Person> personPerformer = ComparisonPerformers.buildGetters(Person.class)
				.build("PersonComparison");
		
		CompositeComparisonPerformer<JobPosition> jobPerformer = ComparisonPerformers.buildGetters(JobPosition.class)
				.configureDynamicPerformer(p -> p.registerDelegate(Person.class, personPerformer))
				.build("JobComparison");
		
		ComparisonResult customResult = jobPerformer.apply(racer, spy);
		ComparisonAssertResult customAssert = ComparisonAssertResult.createWithCause(customResult);
		
		ComparisonResult reflectiveResult = ComparisonPerformers.REFLECTIVE_EQUALS.apply(racer, spy);
		ComparisonAssertResult reflectiveAssert = ComparisonAssertResult.createWithCause(reflectiveResult);
		
		ComparisonResult basicResult = ComparisonPerformers.DEEP_EQUALS.apply(racer, spy);
		ComparisonAssertResult basicAssert = ComparisonAssertResult.createWithCause(basicResult);
		
		ComparisonResult numberResult = ComparisonPerformers.DOUBLE_MATH_EQUALS.apply(BigDecimal.TEN, BigInteger.ONE);
		ComparisonAssertResult numberAssert = ComparisonAssertResult.createWithCause(numberResult);

		AssertError error = new AssertError(Arrays.asList(customAssert, reflectiveAssert, basicAssert, numberAssert));
		error.printStackTrace();
	}
}
