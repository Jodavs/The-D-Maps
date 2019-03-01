# Kodekonventioner

## Navngivning
Generelt skal alle navne være så beskrivende, at man kan forstå indholdet eller funktionen nogenlunde ud fra navnet.

### Klasser
Klassenavne skrives i Pascalcase:

```java
public class SomeClass {}
```

Builder-klassers navn skal slutte med *Builder*:

```java
public class SomeClassBuilder {}
```

### Metoder
Metodenavne skrives i camelcase:

```java
public void someMethod() {}
```

### Felter
Felter skrives i camelcase:

```java
private int someInt;
```

Hvis en feltvariabel er ufærdig eller ufuldstændig prependes navnet med "_":

```java
private String _notComplete;
```

### Lokale variable
Som udgangspunkt frit valg, men skal helst være nogenlunde forståeligt og sammenhængende.

### Konstanter (static final)
Konstanter skrives altid med all-caps med "_" mellem ord:

```java
public static final SOME_CONSTANT;
```

### Loops
**Normalt:** *i*, *j*, *k*, ..., etc.

```java
for (int i=0; i<n; i++) {
	for (int j=0; j<n; j++) {
		for (int k=0; k<n; k++) {
		}
	}
}
```

**Ved reference til koordinater (eg. i matrix):** *x*, *y*, ..., etc.

**Ved reference til rækker og kolonner:** *r*, *c*.

### Parametre
I konstruktorer skal parametre have samme navne som de felter de repræsenterer:

```java
public class Model {
	private int someData;
	
	public Model(int someData) { this.someData = someData }
}
```

Generelt skal parametre skrives i camelcase.

## Struktur

### Placering af tuborgklammer
Den første tuborgklamme efter definition eller control-flow statement skrives på samme linje som dette:

```java
if (something) {
	// code
}
```

Ved meget korte metodedefinitioner (f.eks. gettere / settere) eller korte control-statements:

```java
public String getValue() { return value; }
```

### Field-keywords
Keywords før feltdefinitioner skrives i følgende rækkefølge:

```java
public/private/protected transient static final
```

### Indention 
Al kode skal indenteres med ét tab-tegn pr. niveau:

```java
if (something) {
	while (something-else) {
		// code
	}
}
```

### Gettere/settere
Alle felter i klasser skal være private (undtagen i særlige tilfælde) og skal have en getter og kan have en setter:

```java
private int count;

public int getCount() { return count; }
public void setCount(int count) { this.count = count; }
```

Setteres parametre skal navngives efter feltnavnet den setter. 

### Builder-klasser
Alle klasser, hvis setter-metoder returnerer *this*, og som bruges til at opbygge en anden type objekt, er Builder-klasser.

Eksempel på setter i builder-klasse:

```java
public SomeClassBuilder setCount(int count) { this.count = count; return this; }
```

## Dokumentation / kommentarer

### Kodekommentarer
Normale kommentarer er altid single-line og der skal være mellemrum mellem *//* og kommentaren:

```java
// somecomment
// othercomment
```

### JavaDoc
Al ikke-triviel kode skal dokumenteres med JavaDoc-kommentarer.

Ved beskrivelse af metode:

```java
/**
 * Beskrivelse af funktion
 * gerne flere linjer
 *
 * @param parameternavn beskrivelse
 * @param parameternavn2 beskrivelse
 *
 * @return beskrivelse
 *
 * @throws type beskrivelse
 */
```

Ved beskrivelse af klasser:

```java
/**
 * Beskrivelse af, hvad klassen gør
 */
```