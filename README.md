[![CircleCI](https://circleci.com/gh/stojkovm/isa-deployment-test/tree/master.svg?style=svg)](https://circleci.com/gh/stojkovm/isa-deployment-test/tree/master)
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=stojkovm_isa-deployment-test)

Uputstvo za povezivanje Spring Boot projekta sa GitHuba sa SonarCloud, TravisCI/CircleCI i Heroku servisima.
Ovo je okvirno uputstvo koje možda neće svima odraditi posao.
Ideja je da okvirno prikaže koji se servisi mogu uvezati i koristiti kako bi se povećao kvalitet izrade i održavanja projekta.
Možda će nekome pomoći :blush:

- Ideja
  ![Flow](/assets/flow.png)

# Heroku Deployment

- Kreirati nalog na [Heroku](https://heroku.com)
- Kreirati novu aplikaciju
- Ukoliko želite da koristite Postgres bazu potrebno je otići na Resources tab aplikacije, ići na _Find more add-ons_ i odabrati Heroku Postgres
  ![Find more add-ons](/assets/find_more_addons.png)
  ![Heroku Postgres](/assets/postgres.png)
- Aktivacijom Postgres baze generisaće se konekcioni parametri koji se mogu sakriti iz konfiguracionih fajlova projekta tako što će se otići na _Settings_ tab -> _Reveal Config Vars_ -> i upisati kao _key:value_ parove
  ![Postgres Credentials](/assets/postgres_creds.png)
  Konekcione parametre možete pročitati tako što ćete u _Resources_ tabu kliknuti na _Heroku Postgres_ add-on -> _Settings_ -> i kliknuti na dugme _View Credentials..._ koje se nalazi u _Administration_ sekciji.
  ![Heroku Config Vars](/assets/Heroku_config_vars.png)
- Ažurirati `application.properties` da kredencijali odgovaraju konfiguracionim promenljivama (videti `application.properties` projekta)
- Ako je projekat upakovan kao `war` fajl, potrebno je dodati fajl sa nazivom `Procfile` (bez ekstenzije) u root folder projekta sa sledećim sadržajem:

```
  web: java $JAVA_OPTS -jar target/testing-example-0.0.1-SNAPSHOT.war --server.port=$PORT $JAR_OPTS
```

- U `application.properties` fajl dodati:

```
server.port=${PORT:8080}
```

- Ako GitHub ne prepoznaje projekat kao Java project dodati `system.properties` sa sledećim sadržajem:

```
java.runtime.version=1.8
```

- Na _Deploy_ tabu za _Deployment method_ odabrati GitHub (najlakša varijanta)
- U odeljku _App connected to GitHub_ odabrati projekat sa GitHuba za koji radite deploy
- Manualni deploy možete odraditi u sekciji _Manual deploy_ gde možete da odaberete granu koju želite da deployujete
- Možete uključiti automatski redeloy koji će se aktivirati pri svakom novom commitu
  ![Heroku - Github integration](/assets/heroku_github.png)
- Ukoliko nije aktivan dyno instalirati [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli), ulogovati se kroz terminal i ukucati komandu:

```
$ heroku ps:scale web=1 --app <naziv_aplikacije>
```

# TravisCI

- Napraviti nalog na [Travis CI](https://travis-ci.com/) (povezati se sa GitHubom)
- Odabrati repozitorijum koji treba da uđe u CI ciklus: u gornjem desnom uglu klknuti na profilnu sliku -> otvoriti tab _Settings_ -> kliknuti na dugme _Manage repositories on GitHub_ i odabrati repozitorijum(e) -> kliknuti na _Sync account_ dugme -> u _Repositories_ delu odabrati repozitorijum
  ![Travis CI Repo](/assets/travisci_repo.png)
- Na _Settings_ tabu projekta dodati _Environment Variablu_ kao key:value par Heroku API ključ koji se može naći na Heroku profilu korisnika, na tabu _Account_ pod sekcijom _API Key_
  ![Heroku API Key](/assets/heroku_api_key.png)
  ![Travis CI Environment Variables](/assets/travis_ci_vars.png)
- Možete u _README.md_ ugraditi sličicu sa statusom CI ciklusa (eng. _badge_): ![Status Image](/assets/travis_status_image.png)
- Napraviti `.travis.yml` fajl u root folderu projekta

```
sudo: required
language: java
jdk: oraclejdk8

services:
  - postgresql

before_install:
  - chmod +x mvnw

script:
  - ./mvnw clean install -DskipTests=false -B

dist: trusty

deploy:
  provider: heroku
  api_key: $HEROKU_API_KEY
  app: <naziv_aplikacije>
```

- Otići na Heroku nalog, na tab _Deploy_ aplikacije i u odeljku _Automatic deploys_ štiklirati _Wait for CI to pass before deploy_ kako bi TravisCI odradio deployment na Heroku tek kada build prođe
  ![Travis CI and Heroku Chain](/assets/heroku_wait_ci.png)

# CircleCI

- Napraviti nalog na [Circle CI](https://circleci.com/) (povezati se sa GitHubom)
- Odabrati repozitorijum koji treba da uđe u CI ciklus: u Projects tabu naći repozitorijum i kliknuti na Set Up Project
  ![Circle CI Repo](/assets/circleci_setup_project.png)
- Ući u povezani projekat i klikom na _Project Settings_ preći na tab _Environment Variables_ i dodati _Environment Variable_ kao key:value par Heroku API ključ koji se može naći na Heroku profilu korisnika, na tabu _Account_ pod sekcijom _API Key_
  ![Heroku API Key](/assets/heroku_api_key.png)
- Takođe dodati Heroku naziv aplikacije
  ![Heroku App Name](/assets/circleci_heroku_app_name.png)
  ![Circle CI Environment Variables](/assets/circleci_variables.png)
- Možete u _README.md_ ugraditi sličicu sa statusom CI ciklusa (eng. _badge_) prema uputstvu na [linku](https://circleci.com/docs/2.0/status-badges/)
- Napraviti u `.circleci` folderu `config.yml` fajl i popuniti ga na primer ovako:

```
version: 2.1

jobs:
  build-and-test:
    docker:
      - image: cimg/openjdk:11.0
    steps:
      - checkout
      - run:
          name: Build
          command: mvn -B -DskipTests clean package
      - run:
          name: Test
          command: mvn test

orbs:
  heroku: circleci/heroku@1.2.6
workflows:
  heroku_deploy:
    jobs:
      - build-and-test
      - heroku/deploy-via-git:
          requires:
            - build-and-test
```

- Otići na Heroku nalog, na tab _Deploy_ aplikacije i u odeljku _Automatic deploys_ štiklirati _Wait for CI to pass before deploy_ kako bi TravisCI odradio deployment na Heroku tek kada build prođe
  ![Travis CI and Heroku Chain](/assets/heroku_wait_ci.png)

# SonarCloud

- Napraviti nalog na [SonarCloud](https://sonarcloud.io) (povezati se sa GitHubom)
- U gornjem desnom uglu nalazi se dugme _+_ gde je potrebno odabrati _Analyze new project_ i povezati svoj repozitorijum sa SonarCloudom
  ![Sonar Add New Project](/assets/sonar_add.png)
- Na GitHub nalogu potrebno je dozvoliti repo za analizu (klik na link _GitHub app configuration._ ili na GitHub-u otvoriti _Applications_ tab u podešavanjima korisničkog naloga -> pronaći aplikaciju _SonarCloud_ i kliknuti na dugme _Configure_)
- U podešavanjima korisničkog naloga, u _Security_ tabu generisati i sačuvati **Token**
- U Travis konfiguraciju dodati _Environment Variable_ SONAR_TOKEN i PROJECT_KEY
  ![Travis CI Sonar Env Var](/assets/travis_ci_sonar_vars.png)
- U CircleCI konfiguraciju dodati _Environment Variable_ SONAR_TOKEN i PROJECT_KEY
  ![Circle CI Sonar Env Var](/assets/circleci_sonar_token.png)
- Isključiti automatsku analizu: _Administration_ -> _Analysis Method_
  ![Sonar Analysis Method](/assets/sonar_analysis_method.png)
- Podesiti _New Code definition_
- U _Administration_ -> _Analysis Scope_ definisati pravila za uključivanje/isključivanje tipova fajlova za skeniranje
- Možete u _README.md_ ugraditi i SonarCloud _badge_: _Overview_ tab -> u donjem desnom uglu se nalazi dugme _Get project badges_ -> odabrati željeni format i dodati u _README.md_.

- Ažurirati `.travis.yml` fajl

```
sudo: required
language: java
jdk: oraclejdk11

services:
  - postgresql

before_install:
  - chmod +x mvnw

addons:
  sonarcloud:
  organization: <naziv_organizacije>
  token: $SONAR_TOKEN

script:
  - ./mvnw clean install -DskipTests=false -B
  - ./mvnw sonar:sonar -Dsonar.projectKey=$PROJECT_KEY -Dsonar.organization=<naziv_organizacije> -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN

dist: trusty

deploy:
  provider: heroku
  api_key: $HEROKU_API_KEY
  app: <naziv_aplikacije>
```

- Ažurirati `config.yml` fajl

```
version: 2.1
jobs:
  build-and-test:
    docker:
      - image: cimg/openjdk:11.0
    steps:
      - checkout
      - run:
          name: Build
          command: mvn -B -DskipTests clean package
      - run:
          name: Test
          command: mvn test
      - run:
          name: Analyze on SonarCloud
          command: mvn sonar:sonar -Dsonar.projectKey=$PROJECT_KEY -Dsonar.organization=<naziv_organizacije> -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN

orbs:
  heroku: circleci/heroku@1.2.6
  sonarcloud: sonarsource/sonarcloud@1.0.3
workflows:
  heroku_deploy:
    jobs:
      - build-and-test
      - heroku/deploy-via-git:
          requires:
            - build-and-test
```

- Ako želite da dodate podršku za Code Coverage potrebno je u `pom.xml` fajl dodati plugin

```
  <plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.2</version>
    <executions>
      <execution>
        <id>default-prepare-agent</id>
        <goals>
          <goal>prepare-agent</goal>
        </goals>
      </execution>
      <execution>
        <id>default-report</id>
        <phase>prepare-package</phase>
        <goals>
          <goal>report</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```

Kada je sve povezano ili će sve raditi:
<img src="/assets/bravo.gif" width="243" height="200">
Ili neće:
<img src="/assets/lol.gif" width="243" height="200">
