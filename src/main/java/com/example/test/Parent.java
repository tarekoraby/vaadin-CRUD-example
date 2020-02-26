package com.example.test;

import java.util.Set;

public class Parent {
	private Integer id;
	private String name;
	Set<Person> children;

	public Parent() {
	}

	public Parent(final Integer id, final String name, final Set<Person> children) {
		this.id = id;
		this.name = name;
		this.children = children;
	}

	public Set<Person> getChildren() {
		return this.children;
	}

	public void setChildren(final Set<Person> children) {
		this.children = children;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

}
