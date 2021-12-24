# README dels Cloud Services del Use Case 2: Monitorització de vehicles - T6.4

## 1. Introducció

Aquest arxiu conté tota la informació referent als serveis del cloud desenvolupats a la tasca 6.4 del Projecte 2 de l'agrupació FEMIOT "Monitorització de vehicles".

Aquest cas d'ús consisteix en la integració d’un dispositiu plug&play, modulable i apte per a vehicles de dues rodes, en especial motocicletes de baixa cilindrada, permetent evolucionar l’experiència de l’usuari cap a la monitorització i la gestió de serveis utilitzant electrònica intel·ligent, tecnologies de connectivitat i computació al núvol. 

D'aquesta manera, la tasca 6.4 ha consistit en el desenvolupament  del  mòdul software que permetrà la gestió de les dades provinents d' aquest dispositiu IoT desenvolupat al WP3 del Projecte 1: Dispositiu instal·lat en vehicle. Juntament amb les dades de històrics del estat de la motocicleta emmagatzemades al llarg de l'operació del vehicle, així com altres possibles informacions com l’estat del tràfic, les condicions ambientals, etc., es desenvoluparan aplicacions de diagnòstic de vehicle, serveis a la conducció, a la gestió d’accidents i la gestió d’averies i robatoris.

## 2. Configuració

En aquest apartat es detallarà el procediment per configurar i executar els algorismes desenvolupats en aquesta tasca per poder realitzar una demo. Per permetre una millor repetibilitat i escalabilitat, a més de fer el sistema compatible i homogeni dins del projecte 2 de l'agrupació FEM-IoT, l'arquitectura del software està desenvolupada sobre Docker.

### 2.1 Requeriments

Degut a aquest arquitectura, serà necessari disposar d'un computador amb Sistema Operatiu Linux, amb l'extensió docker-compose, o bé amb Sistema Operatiu Windows amb el aplicatiu Docker Desktop instal·lat.

### 2.2 Execució

Complint els requeriments descrits a la secció anterior, serà necessari desar al computador l'arxiu 'docker-compose.yml' que conté:

````
version: "3.7"

services:
    app:
        image: uc2femiot
        container_name: app
        ports:
            - 8080:8080
        networks: 
            - app_network
        links:
            - influxdb

    influxdb:
        image: influxdb:1.8
        container_name: influxdb
        ports: 
            - 8086:8086
        networks: 
            - app_network
        volumes:
            - prueba-influxdb-data:/var/lib/influxdb
        environment:
            INFLUXDB_ADMIN_USER: femiot
            INFLUXDB_ADMIN_PASSWORD: mcia1234
            INFLUXDB_DB: UC2

    grafana:
        image: grafana/grafana
        container_name: grafana
        ports:
            - 3000:3000
        networks: 
            - app_network
        volumes:
            - prueba-grafana-data:/var/lib/grafana
        links:
            -   influxdb
        
networks: 
    app_network:
volumes:
    prueba-influxdb-data:
    prueba-grafana-data:
````

D’aquesta manera, des de la ubicació de l’arxiu, només cal executar la següent comanda des de una terminal:

````
$ docker-compose up -d
````

Havent realitzat aquests passos, els algorismes estaran executats i corrent contínuament. Amb això, és necessari disposar d’un conjunt de mostres d’entrada per poder obtenir resultats. En aquest **[enllaç](https://drive.google.com/file/d/1-to7Tj8Tg2wzVsixtIjLH4j1w_C93HNR/view?usp=sharing)** es troba un data set de prova, així que només cal importar les dades a InfluxDB per comprovar el funcionament de l’arquitectura.

Per importar aquestes dades només cal executar la següent comanda a la terminal de la computadora o màquina virtual on s'estigui executant el contenidor de InfluxDB.

````
$ influx write -f /path/to/file/UC2VEH.csv
````

## 3. Descripció dels algorismes

L'objectiu d'aquesta secció del README es fer una descripció dels algorismes implementats en els Cloud Services desenvolupats per aquest use case.

La API d'aquest software te accés directe a la base de dades de InfluxDB mencionada a la secció anterior, aixó permet fer queries per obtenir les dades que hagi transmés el dispositiu equipat sobre el vehicle de manera regular. La API ha estat disenyada per detectar quan s'afageixen noves dades per processar-les, produir els outputs desitjats i pujar aquestes noves dades a InfluxDB.

La naturalesa de les dades es de base temps, ja que es produixen i envien les dades del vehicle amb una certa periodicitat. Per això cada cop que es detecta que la base de dades ha sigut actualitzada, es necessari iterar per cada grup de dades que comparteixen timestamp i processar cada un d'aquests paquets. El següent diagrama mostra l'operació general de l'algorisme.

![enter image description here](https://github.com/gorkaunzueta/FEMIOTUC2/blob/main/FEMIOTGen.png)

El processat de les dades es centra al voltant de tres serveis: (3.1) Servei de suport a la conducció, (3.2) servei de manteniment i (3.3) servei de seguretat i protecció.

### 3.1 Servei de suport a la conducció

Aquest servei té com a objectiu fer un anàlisis del perfil de conducció del vehicle per a poder aportar una informació més detallada a algoritmes d’optimització de rutes. D’aquesta manera permet fer una gestió de la operació de una flota de vehicles més eficient i detallada corresponent al vehicle i usuari que pertoqui.

Per a aquest processat, el algorisme requereix de les dades de posició, d’acceleració i de camp magnètic i a més, en cas de ser disponibles per al vehicle corresponent, utilitzarà les dades de bateria o nivell del dipòsit. Amb aquestes dades, el algorisme calcularà dos paràmetres: Acc/fren i desnivell.

L’acceleració en el eix longitudinal del vehicle permet detectar l’estat dinàmic d’aquest. Utilitzant les dades obtingudes del vehicle durant una etapa de calibratge, és possible identificar els diferents ombralls que diferencien si el vehicle es troba en acceleració, en acceleració constant o en frenada. Aquest estat s’emmagatzema en el paràmetre Acc/fren i permet identificar el nombre de frenades i arrencades per a un tram concret en conjunt amb la posició. Així, es pot parametritzar l’estrès que pot comportar una certa ruta per al vehicle.

Per altre banda, utilitzant les dades d’acceleració i camp magnètic en els tres eixos del vehicle es possible obtenir els tres angles d’Euler. A partir del angle al voltant de l’eix transversal, el algorisme calcula el pendent del terreny per poder identificar les condicions del terreny per a la ruta que està realitzant el vehicle.

### 3.2 Servei de manteniment

El servei de manteniment busca fer una monitorització més precisa de l’ús i desgast del vehicle, enfocat a proporcionar aquestes dades a terceres empreses d’assegurances i tallers per permetre que facin ofertes més detallades i personalitzades per a l’usuari gràcies a la caracterització del perfil de conducció.

Per a aquest servei són necessàries les dades de posició, acceleració i bateria o gasolina, en funció de si el vehicle és elèctric o de combustió corresponentment. Com a resultat, el processat retorna paràmetres de recorregut, velocitat i estat de la bateria/motor.

Utilitzant les dades de posició de latitud i longitud de l’instant a processar juntament amb la latitud i longitud de l’instant anterior és possible computar la distancia recorreguda per el vehicle. Aquesta variable serà necessària per altres càlculs d’aquest servei a més de per permetre monitoritzar els kilòmetres recorreguts pel vehicle, proporcionant una dada molt rellevant dins d’aquest servei.

Havent calculat el recorregut és senzill calcular la velocitat mitjana d’aquest trajecte utilitzant els timestamps dels dos instants. Amb la dada de velocitat mitjana del trajecte, permet generar histogrames de velocitat per identificar el règim d’ús del vehicle. El mateix tipus d’histogrames s’obtenen amb les dades d’acceleració i de pendent, permetent obtenir una millor descripció del perfil de conducció rellevant per al manteniment del vehicle.

En el cas de que el vehicle es tracti d’una motocicleta elèctrica, el dispositiu proporcionarà dades del percentatge de bateria del vehicle. Amb aquestes dades, l’algorisme podrà analitzar els cicles de càrrega i descàrrega de la bateria per proporcionar un indicador de l’estat de la bateria. Si es tracta d’un vehicle de combustió, es proporcionarà una dada de nivell del dipòsit de gasolina. Amb aquesta dada es pot monitoritzar l’ús de gasolina del vehicle per identificar l’estrès d’operació del motor i poder fer una millor predicció de la fatiga d’aquest.

### 3.3 Servei de seguretat i protecció

Finalment, el servei de seguretat i protecció aprofita que es tenen aquesta gran quantitat de dades referents al vehicle per identificar possibles robatoris i accidents, permetent una ràpida resposta de les autoritats pertinents quan sigui necessari enviant un avís en un interval de segons des de que succeeix l’esdeveniment.

El servei requereix de les dades d’acceleració i de camp magnètic per a identificar aquests possibles accidents i robatoris, de manera que genera diferents indicadors per a mostrar-ho a l’usuari.

L’algorisme inicia utilitzant les dades d’acceleració per detectar l’estat del vehicle. Havent realitzat una etapa de calibratge permet detectar la vibració de si el motor es troba engegat i a més si es troba en moviment. Així es genera un indicador per identificar en quin dels quatre estat es troba el vehicle: motor on en repòs, motor on en moviment, motor off en repòs i motor off en moviment.

Havent deduït l’estat del vehicle, en el cas de que es trobi en motor off en moviment, l’algorisme fa un anàlisis de l’ús anterior per detectar si esta succeint un robatori i emmagatzema el resultat en un indicador.

Per la detecció d’accidents, el servei segueix dues línies d’anàlisis paral·leles per a augmentar la precisió. Per una banda, utilitza les acceleracions i camp magnètic per a computar els angles d’Euler com s’ha mencionat anteriorment. Seguidament, compara aquests angles amb diversos ombralls que permetin identificar un accident. En el cas que això es compleixi, l’algorisme comprova els últims 10 segons del vehicle per detectar si ha succeït algun pic d’acceleració per determinar que hi ha un accident i informar a les autoritats pertinents.

En paral·lel, el servei calcula la variació d’acceleració respecte l’instant anterior. En el cas de que aquests paràmetres superin uns ombralls determinats per l’etapa de calibratge, el servei compara les dades obtingudes amb un model de ML de detecció de novetat generat específicament per al vehicle. D’aquesta manera permet identificar si ha ocorregut un esdeveniment fora dels límits de normalitat d’ús del vehicle i genera una alerta corresponent.

## 4. Aspectes de seguretat

### 4.1 Algorisme d'anonimització de les dades
La recopilació i processament de dades de caire sensible, tal com la ubicació del vehicle, per tal d’oferir els serveis descrits suposen un risc per la privacitat dels usuaris. Aquest fet, juntament amb la possibilitat d’exportar el resultats del seu processament com a open data, fa necessària la implementació de tècniques que permetin anonimització de les dades un cop aquestes arribin al final del seu cicle de vida útil per al sistema o en permeti la seva exportació de la plataforma complint amb la normativa que dictamina la GDPR.

L’anonimització de dades és un tipus de sanejament de la informació que es centra en la modificació de sets de dades de manera que no es puguin utilitzar per obtenir informació privada sobre les persones físiques a les quals corresponen, però que, a la vegada, aquestes continuïn sent vàlides amb finalitats estadístiques.

D’acord amb l’anàlisi de tècniques d’anonimització presentades en el lliurable 2.3, s’ha optat per la implementació del sistema d’anonimitazació descrit en l’article Microaggregation-and permutation-based anonymization of movement data, el qual es basa en l’ús de tècniques de micro-agregació de trajectòries i la permutació de localitzacions per aconseguir k-anonymity de les trajectòries dels vehicles.

#### 4.1.1 Mètode d’anonimització SwapLocations

El mètode d’anonimització que s’ha implementat rep el nom de SwapLocations, el qual combina l’ús de la tècnica de micro-agregació combinada amb la permutació de localitzacions, per produir trajectories anonimitzades composades per les coordenades, o punts de localització, originals.

Per tal d’anonimitzar un grup de trajectòries, formades per un set de coordenades amb les seves respectives marques temporals, l’algoritme primer agrupa trajectòries, en funció de la seva similitud, en clústers de com a mínim k elements. Amb aquesta finalitat es disposa d’una funció de “distancia” que determina com de similars són dos trajectories en funció de la ubicació espacial i temporal dels punts que les composen. Un cop generats els clústers, el sistema procedeix a anonimitzar individualment cada cluster que contingui més de k trajectòries aplicant el mètode SwapLocations, i en descarta la resta, al considerar que no disposen de trajectòries suficients per evitar una posterior re-identificació.

El mètode SwapLocations té com a objectiu intercambiar el recorregut de de les trajectòries que ha anonimitzar a partir dels les ubicacions, temporals i espacials, que aquestes tenen en comú. SwapLocations s’inicia amb una trajectòria aleatòria del clúster que s’està anonimitzant i intenta agrupar cada ubicació amb ubicacions no hagin estat intercanviades anteriorment pertanyents a altres trajectòries de manera que: (i) les marques de temps d'aquests ubicacions no difereixen més d'un determinat llindar de temps; (ii) les coordenades espacials no difereixen més d'un llindar espacial concret. Si es troben un grup d’ubicacions que compleixen les condicions mencionades, es realitzen intercanvis aleatoris entre les ubicacions del grup format. L'intercanvi aleatori d'aquest grup d’ubicacions garanteix que qualsevol d'aquestes té la mateixa probabilitat de romandre en la seva trajectòria original o convertir-se en un nou triple en qualsevol de les altres trajectòries del cluster. Aquest procés continua fins que no queda cap ubicació en la seva trajectòria original. El procés d’anonimització finalitza quan s’han anonimitzat tots el clústers en que incialment s’ha dividit el dataset original.

Una descripció més acurada sobre el procediment que realitza el l’algorisme SwapLocations per anonimitzar un set de trajectòries es pot trobar en l'article original publicat a la revista científica Information Sciences:

-   Domingo-Ferrer, Josep, and Rolando Trujillo-Rasua. "Microaggregation-and permutation-based anonymization of movement data." Information Sciences 208 (2012): 55-80.

#### 4.1.2 Implementació de l’algorisme SwapLocations

El mecanisme d’anonimització de trajectòries SwapLocations descrit en els apartats anteriors, juntament amb les eines de càlculs de distàncies entre trajectories i agrupació en clústers, s’han implementat agrupats en un mateix mòdul sobre el llenguatge de programació interpretat “Python”. Aquest codi es pot consultar en el següent enllaç de github: **[enllaç]()**

Per tal de fer ús d’aquest mòdul en l’entorn de runtime definit en per el cas d’ús 6.4 “Mòdul de suport a la conducció”, s’ha implementat una funció principal on es fixa la configuració i la metodologia més adient per la anonimització de trajectòries que requereixen les dades recollides per el cas d’ús. L’ús final del mecanisme d’anonimització implementat requereix que les trajectòries a anonimitzar estiguin emmagatzemades en un fitxer .CSV, que l’algorisme rebrà com a paràmetre d’entrada, formatejat de la manera següent:

````
lat,lon,timestamp,user_id

37.75331,-122.42591,2008/06/08 09:56:25,1
37.75472,-122.42329,2008/06/08 09:55:36,1
37.75676,-122.42354,2008/06/08 09:54:34,1
37.76627,-122.41985,2008/06/08 09:50:14,2
37.77182,-122.42035,2008/06/08 09:49:23,2
37.77642,-122.41466,2008/06/08 09:47:21,2
...
````

El fitxer d’entrada conté la informació dels punts de totes les trajectòries a anonimitzar. Cada punt representa una entrada o fila en el fitxer, i conté la seva geo-localització (latitud i longitud), una marca temporal i l’identificador del usuari que va registrar la coordenada.

A partir d’aquesta informació, l’algoritme determina les trajectòries de cada usuari, les anonimitza i genera un nou dataset amb els el punts de les noves trajectories anonimitzades. El dataset anonimitzat s’exporta com un fitxer .CSV conservant el mateix format que el fitxer original.

### 4.2 Protecció de la confidencialitat i integritat de les dades

L’esquema del cas d’ús proposa una sèrie de vehicles embarcats dotats amb un dispositiu integrat que recol·lecta dades sobre la conducció del seu usuari. Aquests dispositius transmeten les dades recol·lectades cap el servidor d’aplicacions o backend per mitjà de la connectivitat que proveeixen les RSUs desplegades per plataforma del projecte FemIoT. Aquest esquema implica que dades sensibles dels usuaris que fan ús dels del cas d’ús, tals com la seva ubicació, es transfereixen a traves un entorn no segur com Internet. Davant aquesta situació és necessari que aquest procés estigui securitzat i es garanteixi la confidencialitat i integritat de les dades durant el mateix.

#### 4.2.1 Confidencialitat i integritat

L’escenari descrit menciona dos punts de comunicació amb possibles requeriments d’aplicar mesures de securització: i) Dispositiu – RSU(Gateway); ii) RSU(Gateway) – Servidor d’aplicacions.

**Comunicació entre Dispositiu – RSU(Gateway)**

D’acord amb les especificacions de la implementació el sistema fa ús tecnologia WiFi per interconectar els vehicles amb les RSUs, fent servir aquests últimes com a punts d’accés a Internet. La tecnologia  WIFI, emprant el protocol WPA2, proporciona seguretat a nivell d’enllaç, garantint la confidencialitat i integritat de les comunicacions, a tota entitat que coneix la contrasenya. Amb la configuració descrita, el sistema pot garantir aquestes propietats en l’escenari descrit.

**Comunicació entre RSU(Gateway) – servidor d’aplicacions**

Independentment del nivell de securització que la tecnologia WiFi proveeix a nivell d’enllaç, les comunicacions entre les RSUs i el servidor d’aplicacions creuen un entorn insegur i és necessari que estiguin degudament securitzades. D’acord amb les especificacions del cas d’ús, el servidor d’aplicacions, amb independència de la seva ubicació, proveeix el seus serveis per mitjà d’una interfície REST, amb el que resulta mandatori que els dispositius embarcats només hi estableixin comunicacions segures per mitjà HTTPS. De la mateixa manera, la interfície REST del servidor ha d’estar configurada de manera que únicament accepti connexions HTTPS, rebutjant-ne les no segures. Seguint aquest desplegament es poden garantir les propietats de confidencialitat i integritat mencionades anteriorment.

### 4.3 Autenticitat
Davant de l’escenari que proposa el cas d’ús, és necessari que el sistema garanteixi l’autenticitat dels missatges enviats entre els dispositius embarcats i el servidor d’aplicacions. Aquesta mesura és necessària per evitar que qualsevol entitat conectada a la xarxa pugui suplantar els vehicles de la flota i enviar dades en nom seu amb finalitats malicioses.

L’ús certificats digitals, i la seva corresponent parella de claus, emesos per una entitat de confiança, és una pràctica habitual que permet a les entitats participants generar signatures digitals. La generació d’aquestes signatures permet garantir la integritat i autenticitat dels missatges enviats, evitant els atacs de suplantació prèviament mencionats. No obstant, les presents limitacions del microcontrolador PIC32MZ2048EFM144, 200 MHz i 2048 KB de memòria flash, que integren els dispositius embarcats no permet la implementació d’aquesta solució.

En aquest tipus d’escenari, per tal que el servidor d’aplicacions pugui garantir l’autenticitat dels missatges rebuts, s’ha d’introduir una clau simètrica hardcoded en el codi de cada dispositiu que també ha de ser coneguda pel backend o servidor. Amb aquest desplegament, els dispositius generen un “keyed hash” de cada missatge que s’envia al servidor. Aquest, un cop rebut el missatge i el hash, intenta reconstruir el mateix hash a partir del missatge i la suposada clau del dispositiu que l’envia. Si l’operació finalitza amb èxit el servidor pot asumir l’autenticitat del missatge. Per aquesta operació s’ha seleccionat la primitiva HMAC( K , m ) on K es la clau secreta i m el missatge a enviar.

Un altre aspecte a considerar davant la possibilitat de desplegament del cas d’ús en entorns reals quan s'assoleixin nivells de TRL6 o superiors és l’adopció de WiFi versió enterprise. El WPA-Enterprise és un mode per a xarxes inalàmbriques que proporciona un control individualitzat i centralitzat sobre l'accés a la xarxa WiFi. Amb aquest mode, tot usuari o dispositiu que intenta connectar-se a la xarxa ha de presentar les seves credencials d'accés al sistema de manera que sigui autentificat per un servidor central. El desplegament d’aquest sistema complementa els mecanismes descrits anteriorment i ajuda a mitigar els possibles atacs de suplantació identificats.
