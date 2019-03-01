package edu.itu.the_d.map.view;

/**
 * Types used by searching and showing suggestions
 * <p>
 * Copyright 2016 The-D
 */
public enum SuggestionType {
	STREET("Vej"), CITY("By"), POSTCODE("Postnummer"), POI("Sted");
	public String typeName;

	SuggestionType(String typeName) {
		this.typeName = typeName;
	}
}