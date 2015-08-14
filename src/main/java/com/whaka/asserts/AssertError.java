package com.whaka.asserts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Required to have at least one {@link AssertResult}.
 * @see #getResults()
 */
public class AssertError extends Error {

	private final List<AssertResult> results;
	
	public AssertError(Collection<AssertResult> results) {
		super(formatMessage(results));
		Preconditions.checkArgument(results.size() > 0, "At least one assert result is required!");
		this.results = Collections.unmodifiableList(new ArrayList<>(results));
	}
	
	public List<AssertResult> getResults() {
		return results;
	}
	
	private static String formatMessage(Collection<AssertResult> results) {
		try (StringWriter sw = new StringWriter(); BufferedWriter out = new BufferedWriter(sw)) {
			out.write("Assertion fail!");
			for (AssertResult result: results) {
				for (String line : result.toString().split("[\r\n]+")) {
					out.newLine();
					out.write("\t");
					out.write(line);
				}
			}
			out.flush();
			return sw.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getResults());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			AssertError that = (AssertError) object;
			return Objects.equal(getResults(), that.getResults());
		}
		return false;
	}
}