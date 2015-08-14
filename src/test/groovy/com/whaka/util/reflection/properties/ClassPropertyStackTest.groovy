package com.whaka.util.reflection.properties

import spock.lang.Specification

class ClassPropertyStackTest extends Specification {

	def "construction"() {
		when:
			def stack1 = new ClassPropertyStack(new ClassPropertyKey("qwe", String))
		then:
			checkStack(stack1, null, "qwe", String)
			stack1.toCallString() == "String#qwe"
			stack1.toLongCallString() == "String#qwe"

		when:
			def stack2 = new ClassPropertyStack(stack1, new ClassPropertyKey("rty", Integer))
		then:
			checkStack(stack2, stack1, "rty", Integer)
			stack2.toCallString() == "String#qwe.rty"
			stack2.toLongCallString() == "String#qwe->Integer#rty"

		when:
			def stack3 = new ClassPropertyStack(stack2, new ClassPropertyKey(12, Object))
		then:
			checkStack(stack3, stack2, 12, Object)
			stack3.toCallString() == "String#qwe.rty.12"
			stack3.toLongCallString() == "String#qwe->Integer#rty->Object#12"

		when:
			def stack4 = new ClassPropertyStack(stack1, new ClassPropertyKey("pop"))
		then:
			checkStack(stack4, stack1, "pop", null)
			stack4.toCallString() == "String#qwe.pop"
			stack4.toLongCallString() == "String#qwe->?#pop"

		when:
			new ClassPropertyStack(null)
		then:
			thrown(NullPointerException)
	}

	def "hashCode/equals"() {
		when:
			def stack1_1 = new ClassPropertyStack(new ClassPropertyKey("qwe", String))
			def stack1_2 = new ClassPropertyStack(new ClassPropertyKey("qwe", String))
		then:
			stack1_1.equals(stack1_2)
			stack1_1.hashCode() == stack1_2.hashCode()

		when:
			def stack2_1 = new ClassPropertyStack(stack1_1, new ClassPropertyKey("rty", Object))
			def stack2_2 = new ClassPropertyStack(stack1_2, new ClassPropertyKey("rty", Object))
		then:
			stack2_1.equals(stack2_2)
			stack2_1.hashCode() == stack2_2.hashCode()

		when:
			def stack1_3 = new ClassPropertyStack(new ClassPropertyKey("pop", Integer))
			def stack1_4 = new ClassPropertyStack(new ClassPropertyKey("lol", Object))
		then:
			stack1_3.equals(stack1_4) == false

		when:
			def stack2_3 = new ClassPropertyStack(stack1_3, new ClassPropertyKey("rty", Object))
			def stack2_4 = new ClassPropertyStack(stack1_4, new ClassPropertyKey("rty", Object))
		then:
			stack2_3.equals(stack2_4) == false

		when:
			def stack2_5 = new ClassPropertyStack(stack1_1, new ClassPropertyKey("pop", Object))
			def stack2_6 = new ClassPropertyStack(stack1_2, new ClassPropertyKey("lol", Object))
		then:
			stack2_5.equals(stack2_6) == false
	}

	def "create-stack"() {
		given:
			def key1 = new ClassPropertyKey("rty")
			def key2 = new ClassPropertyKey("qwe", Object)
			def key3 = new ClassPropertyKey(1.5, String)

		expect:
			ClassPropertyStack.createStack() == null
		and:
			ClassPropertyStack.createStack(key1) == new ClassPropertyStack(key1)
		and:
			ClassPropertyStack.createStack(key1, key2) == new ClassPropertyStack(new ClassPropertyStack(key1), key2)
		and:
			ClassPropertyStack.createStack(key3, key2, key1) ==
				new ClassPropertyStack(new ClassPropertyStack(new ClassPropertyStack(key3), key2), key1)
	}

	void checkStack(ClassPropertyStack stack, ClassPropertyStack parent, Object id, Class<?> declaringClass) {
		assert stack.getParent().is(parent)
		assert stack.getValue().getId() == id
		assert stack.getValue().getDeclaringClass() == declaringClass
	}
}
