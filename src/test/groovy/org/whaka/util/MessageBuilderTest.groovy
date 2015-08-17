package org.whaka.util

import spock.lang.Specification

class MessageBuilderTest extends Specification {

	def "build message with parameters"() {
		expect:
			MessageBuilder.build(message, parameters).equals(result)
		where:
			message		|	parameters						||	result
			""			|	[:]								||	""
			null		|	[:]								||	""
			""			|	null							||	""
			null		|	null							||	""
			""			|	["q": 12]						||	"[q=12]"
			""			|	["q": 12, "w": 22]				||	"[q=12, w=22]"
			"qwe"		|	["q": 12, "w": 22]				||	"qwe[q=12, w=22]"
			"qwe"		|	["q": null, "w": false]			||	"qwe[q=null, w=false]"
			"qwe"		|	[:]								||	"qwe"
			"qwe"		|	null							||	"qwe"
	}

	def "build no parameters"() {
		given:
			MessageBuilder builder = new MessageBuilder()
			Object[] arguments = args
		expect:
			builder.build(message, arguments).equals(result)
		where:
			message		|	args							||	result
			""			|	[]								||	""
			null		|	[]								||	""
			""			|	null							||	""
			null		|	null							||	""
			""			|	["q", 12]						||	""
			"qwe"		|	["q", 12]						||	"qwe"
			"qwe%s%s"	|	["q", 12]						||	"qweq12"
			"%d"		|	[12, 22]						||	"12"
	}

	def "build with parameters"() {
		given:
			MessageBuilder builder = new MessageBuilder()

		when:
			builder.getParameters().put("qwe", 12)
		then:
			builder.build("qaz").equals("qaz[qwe=12]")

		when:
			builder.getParameters().put("pop", null)
		then:
			builder.build("lol").equals("lol[qwe=12, pop=null]")

		when:
			builder.getParameters().put("er", false)
		then:
			builder.build("sup").equals("sup[qwe=12, pop=null, er=false]")
			builder.build("").equals("[qwe=12, pop=null, er=false]")
			builder.build(null).equals("[qwe=12, pop=null, er=false]")
			builder.build("_%s_", true).equals("_true_[qwe=12, pop=null, er=false]")

		when:
			builder.getParameters().remove("pop")
		then:
			builder.build("sup").equals("sup[qwe=12, er=false]")

		when:
			builder.getParameters().clear()
		then:
			builder.build("sup").equals("sup")
			builder.build("sup%dm", 12).equals("sup12m")
	}

	def "put parameter"() {
		given:
			MessageBuilder builder = new MessageBuilder()

		when:
			builder.putParameter("qwe", 12)
		then:
			builder.getParameters().equals(["qwe":12])

		when:
			builder.putParameter("pop", null)
		then:
			builder.getParameters().equals(["qwe":12, "pop":null])

		when:
			builder.putParameter("sup", false)
		then:
			builder.getParameters().equals(["qwe":12, "pop":null, "sup":false])
	}
}
