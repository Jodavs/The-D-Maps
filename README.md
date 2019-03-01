<p align="center">
  <img src ="images/poster.png">
</p>


## Release v0.10
To run the project run the jar file in the v0.10 release.

###Change log 
* Dijkstra
* Hurtigste rute
* Ensrettede veje
* Tager nu højde for trafikant type (gå/cykle/køre)
* Rutevejledning
* Kortet tegner nu flere typer
* En fejl hvor specifikke relations ikke blev tegnet er blevet fikset
* En fejl hvor “smooth panning” gjorde, at nogle tiles ikke blev tegnet, er blevet fikset.
* Store veje skalere nu i forhold til zoomlevel.
* Panning’s timer er nu meget billigere og derfor mere lækker.
* Loading timeren viser nu den korrekte tid.
* Maptiles bruger nu kun ét BufferedImage til tegning af kortet
* MapView bruger en mere konservativ måde at gemme gamle billeder på
* Rød vej til rutevejledning forårsager ikke længere at store røde firkanter tegnes rundt om kortet
* Ved adressesøgning flyttes kortet nu hen til det punkt adressen er på
* Nearest neighbor virker med panning
* Loading af kort bruger væsentligt mindre hukommelse
* Loading af zip file.
* Lavet GUI til rutevejledning
* Fixet controller opsætning for at opretholde MVC modellen
* Fixet forskellige farver til rutevejledning
* Nyan Cat og Batman Tema implementeret
* Implementeret regular expressions i adressesøgningen


### Known bugs
* Default .obj i jar fil
* JavaDocs!!
* Fikse tileImages (flimren)
* Skal kunne sætte, slette og navngive location pins og gemme dem i .obj og loade dem automatisk
* Skal kunne loade danmarkskort!
* Fixe søgning
* Dijkstra grafen skal tegnes som et MapObject


## Screenshot of 0.10

<div style="text-align:center"><img src ="images/screenshot.png" /></div>
