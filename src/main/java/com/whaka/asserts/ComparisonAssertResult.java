package com.whaka.asserts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.whaka.util.reflection.comparison.ComparisonFail;
import com.whaka.util.reflection.comparison.ComparisonResult;
import com.whaka.util.reflection.comparison.ComplexComparisonResult;
import com.whaka.util.reflection.properties.ClassPropertyStack;

public class ComparisonAssertResult extends AssertResult {

	private ComparisonResult comparisonResult;

	/**
	 * Analogue of the {@link #ComparisonAssertResult(ComparisonResult, String)} with null message.
	 */
	public ComparisonAssertResult(ComparisonResult comparisonResult) {
		this(comparisonResult, null);
	}
	
	/**
	 * <p>Creates instance of the assert result with specified comparison result and message.
	 * 
	 * <p><b>Note:</b> 'cause' field is not affected by this constructor. If specified result is an instance
	 * of the {@link ComparisonFail} and you want to copy it's cause to result - use {@link #createWithCause(ComparisonResult, String)}
	 * method.
	 */
	public ComparisonAssertResult(ComparisonResult comparisonResult, String message) {
		super(message);
		Objects.requireNonNull(comparisonResult, "Comparison result cannot be null!");
		Preconditions.checkArgument(!comparisonResult.isSuccess(), "Assert shouldn't be created from successful result!");
		this.comparisonResult = comparisonResult;
	}

	/**
	 * Analague of {@link #createWithCause(ComparisonResult, String)} with null message.
	 */
	public static ComparisonAssertResult createWithCause(ComparisonResult result) {
		return createWithCause(result, null);
	}
	
	/**
	 * Method additionally checks whether specified result is an instance of the {@link ComparisonFail}
	 * and if so - copies cause exception from the comparison result to assert result.
	 */
	public static ComparisonAssertResult createWithCause(ComparisonResult result, String message) {
		ComparisonAssertResult assertResult = new ComparisonAssertResult(result, message);
		if (result instanceof ComparisonFail)
			assertResult.setCause(((ComparisonFail) result).getCause());
		return assertResult;
	}
	
	public ComparisonResult getComparisonResult() {
		return comparisonResult;
	}
	
	@Override
	public Object getActual() {
		return getComparisonResult().getActual();
	}
	
	@Override
	public Object getExpected() {
		return getComparisonResult().getExpected();
	}

	@Override
	public void setActual(Object actual) {
		throw new UnsupportedOperationException("Cannot set actual value to performed result!");
	}
	
	@Override
	public void setExpected(Object expected) {
		throw new UnsupportedOperationException("Cannot set expected value to performed result!");
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getComparisonResult());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ComparisonAssertResult that = (ComparisonAssertResult) object;
			return Objects.equals(getMessage(), that.getMessage())
				&& Objects.equals(getCause(), that.getCause())
				&& Objects.equals(getComparisonResult(), that.getComparisonResult());
		}
		return false;
	}
	
	@Override
	public String toString() {
		String head = formatHeadString();
		ComparisonResult comparisonResult = getComparisonResult();
		if (comparisonResult instanceof ComplexComparisonResult)
			return formatComplexResult(head, (ComplexComparisonResult) comparisonResult);
		return head;
	}
	
	private String formatHeadString() {
		ToStringHelper tsh = MoreObjects.toStringHelper(this)
			.add("actual", getActual())
			.add("expected", getExpected());
		if (getMessage() != null)
			tsh.add("message", getMessage());
		if (getCause() != null)
			tsh.add("cause", getCause().getClass());
		tsh.add("performer", getComparisonResult().getComparisonPerformer());
		return tsh.toString();
	}
	
	private static String formatComplexResult(String head, ComplexComparisonResult result) {
		try (StringWriter sw = new StringWriter(); BufferedWriter out = new BufferedWriter(sw)) {
			out.write(head);
			for (Map.Entry<ClassPropertyStack, ComparisonResult> e : result.flatten().entrySet()) {
				if (e.getValue().isSuccess())
					continue;
				out.newLine();
				out.write("\t");
				out.write(e.getKey().toCallString());
				out.write(" = ");
				out.write(e.getValue().toString());
			}
			out.flush();
			return sw.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
