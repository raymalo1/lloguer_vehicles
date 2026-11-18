# Lloguer de vehicles Prisitia

Aplicació web que fa de **ERP i CRM** per a una empresa de reserva de vehicles. Es tracta d'un producte mínim viable (PMV) que centralitza en un sol lloc la gestió de clients, vehicles i reserves, i substitueix el paper i les trucades per un sistema on tot queda registrat.

## Què permet fer

- **Els clients** es registren a la web, esperen que un administrador validi el seu registre i, un cop aprovats, consulten els vehicles disponibles per unes dates i fan la reserva. El vehicle queda bloquejat per aquell període i el client rep un codi de reserva.
- **Els agents** gestionen clients, vehicles i reserves. Quan arriba el client, lliuren el vehicle i en documenten l'estat amb fotografies; quan el retorna, comproven si hi ha desperfectes i decideixen si es reté la fiança.
- **L'administrador** té accés total, valida els registres de nous clients, gestiona els agents i consulta els informes de reserves per client.

## Com funciona

L'aplicació és accessible des d'un navegador, pot ser utilitzada per diversos usuaris alhora i guarda les dades en dues bases de dades: una de relacional (**MySQL**) per a la informació estructurada, i una de no relacional (**MongoDB**) per a l'històric de reserves.



## Context

Projecte de 2DAM (matí) de l'Institut Nicolau Copèrnic, curs 2026-27.