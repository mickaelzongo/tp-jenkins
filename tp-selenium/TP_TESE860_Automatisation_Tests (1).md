# TP — Automatisation des tests
### TESE860 · Mastère EISI 1ère année · Semestre 8

---

> **Point de départ.** Tu as déjà : Jenkins fonctionnel, le projet Maven `tp-jenkins` avec ses tests JUnit 5, et un pipeline CI opérationnel (ICDE848).
>
> **Ce que tu vas construire dans ce TP.** Des tests UI automatisés en Python avec Selenium sur une vraie application web, une nouvelle fonctionnalité développée en TDD sur le projet existant, du mocking de dépendances avec Mockito, et l'intégration de tout ça dans le pipeline Jenkins.

---

## Organisation du TP

| Partie | Sujet | Prérequis |
|--------|-------|-----------|
| I | Introduction et environnement Python/Selenium | Aucun |
| II | Écriture de cas de test Selenium | Partie I |
| III | TDD sur le projet boutique + Mocking | ICDE848 |
| IV | Gestion des données de test + intégration CI | Parties II et III |

---

## Partie I — Introduction à l'automatisation des tests

### 1.1 Tests manuels vs tests automatisés

| Critère | Test manuel | Test automatisé |
|---------|------------|----------------|
| Vitesse d'exécution | Lente | Très rapide |
| Répétabilité | Variable (erreur humaine) | Identique à chaque run |
| Coût initial | Faible | Élevé (écriture du script) |
| Coût à long terme | Élevé (re-exécution) | Faible |
| Adapté pour | Exploratoire, UX | Régression, smoke tests |
| Limite | Fatigue, oublis | Ne détecte pas l'inattendu |

> **Règle pratique.** On automatise ce qui est répété souvent et ce qui doit être stable. On garde le test manuel pour ce qui change ou ce qui nécessite un jugement humain.

### 1.2 Les niveaux de tests

```
        ▲ Lent / Coûteux / Peu nombreux
        │
        │  ┌─────────────────────────┐
        │  │    Tests UI / E2E        │  ← Selenium (ce TP, Partie II)
        │  │  (bout en bout)          │
        │  └─────────────────────────┘
        │  ┌─────────────────────────┐
        │  │   Tests d'intégration   │  ← Failsafe (ICDE848, TP3)
        │  └─────────────────────────┘
        │  ┌─────────────────────────┐
        │  │    Tests unitaires      │  ← JUnit / Surefire (ICDE848, TP1)
        │  └─────────────────────────┘
        │
        ▼ Rapide / Peu coûteux / Très nombreux
```

La pyramide dit : **beaucoup de tests unitaires, quelques tests d'intégration, peu de tests UI**. Les tests UI sont fragiles (un changement de layout les casse) et lents. On les réserve aux parcours critiques.

### 1.3 Outils du marché

| Outil | Langage | Usage principal |
|-------|---------|----------------|
| **Selenium** | Python, Java, JS… | Tests UI navigateur |
| **JUnit 5** | Java | Tests unitaires/intégration |
| **TestNG** | Java | Alternative à JUnit, plus de config |
| **Cucumber** | Multi | BDD (tests en langage naturel) |
| **Pytest** | Python | Tests unitaires Python |
| **Playwright** | Python, JS | Alternatif moderne à Selenium |
| **Mockito** | Java | Mocking de dépendances |

---

### 1.4 Installer l'environnement Python/Selenium

#### Prérequis système
- Python 3.10+ (`python3 --version`)
- pip (`pip3 --version`)
- Google Chrome installé

#### Installation

```bash
# Créer un dossier dédié aux tests UI (hors du projet Maven)
mkdir tp-selenium && cd tp-selenium

# Créer un environnement virtuel Python
python3 -m venv venv

# Activer l'environnement
source venv/bin/activate        # Linux / Mac
venv\Scripts\activate.bat       # Windows

# Installer Selenium
pip install selenium

# Vérifier l'installation
python -c "import selenium; print(selenium.__version__)"
```

#### ChromeDriver

Selenium a besoin d'un driver pour piloter Chrome. Depuis Selenium 4.6, il se télécharge automatiquement via le **Selenium Manager** — tu n'as plus rien à installer manuellement.

**Vérification rapide :**

```python
# test_install.py
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

options = Options()
options.add_argument("--headless")   # pas de fenêtre graphique

driver = webdriver.Chrome(options=options)
driver.get("https://www.google.com")
print("Titre :", driver.title)
driver.quit()
```

```bash
python test_install.py
# → Titre : Google
```

Si tu obtiens le titre, l'environnement est prêt.

---

## Partie II — Écriture de cas de test Selenium

### L'application de test : The Internet

L'URL `https://the-internet.herokuapp.com` est une application web conçue pour les formations Selenium. Elle expose des pages avec des composants réels : formulaires, alertes, drag & drop, upload, etc. C'est la référence utilisée dans les formations officielles Selenium.

Pas besoin de déployer quoi que ce soit — elle est accessible en ligne.

---

### 2.1 Structure d'un test Selenium

Un test Selenium suit toujours la même séquence :

```
1. Créer le driver        → ouvrir le navigateur
2. Naviguer               → driver.get(url)
3. Localiser un élément   → find_element(By.xxx, "valeur")
4. Interagir              → click(), send_keys(), etc.
5. Asserter               → assertEqual, assertTrue, etc.
6. Fermer le driver       → driver.quit()
```

Les **locators** permettent de trouver un élément dans le DOM :

| Locator | Exemple | Quand l'utiliser |
|---------|---------|-----------------|
| `By.ID` | `By.ID, "username"` | En premier — le plus fiable |
| `By.NAME` | `By.NAME, "password"` | Si pas d'ID |
| `By.CSS_SELECTOR` | `By.CSS_SELECTOR, ".btn-primary"` | Flexible, largement supporté |
| `By.XPATH` | `By.XPATH, "//button[@type='submit']"` | En dernier recours |
| `By.LINK_TEXT` | `By.LINK_TEXT, "Se connecter"` | Pour les liens uniquement |

> **Bonne pratique.** Préfère `By.ID` et `By.CSS_SELECTOR`. Évite les XPath longs et fragiles comme `//div[3]/span[2]/a` — ils cassent au moindre changement de structure HTML.

---

### 2.2 Créer la structure du projet

```
tp-selenium/
├── venv/
├── pages/
│   ├── __init__.py
│   ├── login_page.py
│   └── checkboxes_page.py
├── tests/
│   ├── __init__.py
│   ├── test_login.py
│   └── test_checkboxes.py
├── conftest.py
└── requirements.txt
```

Crée les dossiers et fichiers vides :

```bash
mkdir pages tests
touch pages/__init__.py tests/__init__.py
touch pages/login_page.py pages/checkboxes_page.py
touch tests/test_login.py tests/test_checkboxes.py
touch conftest.py requirements.txt
```

Génère le fichier `requirements.txt` :

```bash
pip freeze > requirements.txt
```

---

### 2.3 Le pattern Page Object Model (POM)

Mettre tout le code Selenium dans les tests est une mauvaise pratique : si le HTML change, tu dois modifier chaque test. Le **Page Object Model** sépare la navigation (dans des classes Page) des assertions (dans les tests).

```
┌──────────────────┐         ┌──────────────────┐
│   Test           │ utilise │   Page Object    │
│                  │ ──────► │                  │
│  Ce qu'on vérifie│         │  Comment naviguer│
│  (assertions)    │         │  (locators)      │
└──────────────────┘         └──────────────────┘
```

---

### 2.4 Configurer le driver partagé (conftest.py)

`conftest.py` est un fichier spécial de Pytest. Les **fixtures** qu'il définit sont disponibles dans tous les tests sans import.

```python
# conftest.py
import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

BASE_URL = "https://the-internet.herokuapp.com"

@pytest.fixture
def driver():
    """
    Fixture Pytest : crée un driver Chrome avant chaque test,
    le ferme après — même si le test échoue.
    """
    options = Options()
    options.add_argument("--headless")          # sans interface graphique
    options.add_argument("--no-sandbox")        # requis dans certains environnements CI
    options.add_argument("--disable-dev-shm-usage")

    driver = webdriver.Chrome(options=options)
    driver.implicitly_wait(5)                   # attendre 5s max pour trouver un élément

    yield driver                                # le test s'exécute ici

    driver.quit()                               # toujours exécuté après le test


@pytest.fixture
def base_url():
    return BASE_URL
```

---

### 2.5 TP2-A — Page de login (formulaire)

La page `https://the-internet.herokuapp.com/login` contient un formulaire avec un champ username, un champ password, et un bouton de connexion.

Identifiants valides : `tomsmith` / `SuperSecretPassword!`

#### Étape 1 — Créer le Page Object

```python
# pages/login_page.py
from selenium.webdriver.common.by import By


class LoginPage:
    """
    Encapsule tous les locators et actions de la page de login.
    Les tests n'ont pas à connaître les sélecteurs CSS.
    """

    URL = "/login"

    # Locators — définis comme attributs de classe
    USERNAME_INPUT  = (By.ID, "username")
    PASSWORD_INPUT  = (By.ID, "password")
    LOGIN_BUTTON    = (By.CSS_SELECTOR, "button[type='submit']")
    FLASH_MESSAGE   = (By.ID, "flash")

    def __init__(self, driver, base_url):
        self.driver   = driver
        self.base_url = base_url

    def open(self):
        """Navigue vers la page de login."""
        self.driver.get(self.base_url + self.URL)
        return self

    def entrer_username(self, username):
        self.driver.find_element(*self.USERNAME_INPUT).clear()
        self.driver.find_element(*self.USERNAME_INPUT).send_keys(username)
        return self

    def entrer_password(self, password):
        self.driver.find_element(*self.PASSWORD_INPUT).clear()
        self.driver.find_element(*self.PASSWORD_INPUT).send_keys(password)
        return self

    def cliquer_connexion(self):
        self.driver.find_element(*self.LOGIN_BUTTON).click()
        return self

    def get_message_flash(self):
        """Retourne le texte du message de feedback (succès ou erreur)."""
        return self.driver.find_element(*self.FLASH_MESSAGE).text

    def get_url_courante(self):
        return self.driver.current_url

    # Méthode de haut niveau — combine les actions élémentaires
    def se_connecter(self, username, password):
        return (self
                .entrer_username(username)
                .entrer_password(password)
                .cliquer_connexion())
```

#### Étape 2 — Écrire les tests

```python
# tests/test_login.py
import pytest
from pages.login_page import LoginPage


class TestLogin:
    """
    Tests de la page de login.
    Pattern AAA : Arrange / Act / Assert (Given / When / Then).
    """

    def test_connexion_valide_redirige_vers_securise(self, driver, base_url):
        """
        GIVEN la page de login
        WHEN je saisis des identifiants valides
        THEN je suis redirigé vers /secure
        """
        # GIVEN
        page = LoginPage(driver, base_url)
        page.open()

        # WHEN
        page.se_connecter("tomsmith", "SuperSecretPassword!")

        # THEN
        assert "/secure" in page.get_url_courante(), \
            f"Redirection attendue vers /secure, URL obtenue : {page.get_url_courante()}"

    def test_connexion_valide_affiche_message_succes(self, driver, base_url):
        """
        GIVEN la page de login
        WHEN je saisis des identifiants valides
        THEN un message de succès est affiché
        """
        # GIVEN
        page = LoginPage(driver, base_url)
        page.open()

        # WHEN
        page.se_connecter("tomsmith", "SuperSecretPassword!")

        # THEN
        message = page.get_message_flash()
        assert "You logged into a secure area" in message, \
            f"Message attendu non trouvé. Message reçu : {message}"

    def test_mot_de_passe_incorrect_affiche_erreur(self, driver, base_url):
        """
        GIVEN la page de login
        WHEN je saisis un mot de passe incorrect
        THEN un message d'erreur est affiché (pas de redirection)
        """
        # GIVEN
        page = LoginPage(driver, base_url)
        page.open()

        # WHEN
        page.se_connecter("tomsmith", "mauvaismdp")

        # THEN
        message = page.get_message_flash()
        assert "Your password is invalid" in message, \
            f"Message d'erreur attendu non trouvé. Message reçu : {message}"

    def test_username_incorrect_affiche_erreur(self, driver, base_url):
        """
        GIVEN la page de login
        WHEN je saisis un username inconnu
        THEN un message d'erreur est affiché
        """
        # GIVEN
        page = LoginPage(driver, base_url)
        page.open()

        # WHEN
        page.se_connecter("utilisateur_inconnu", "SuperSecretPassword!")

        # THEN
        message = page.get_message_flash()
        assert "Your username is invalid" in message, \
            f"Message d'erreur attendu non trouvé. Message reçu : {message}"

    def test_champs_vides_restent_sur_page_login(self, driver, base_url):
        """
        GIVEN la page de login
        WHEN je soumets le formulaire sans rien remplir
        THEN je reste sur la page de login
        """
        # GIVEN
        page = LoginPage(driver, base_url)
        page.open()

        # WHEN
        page.cliquer_connexion()

        # THEN
        assert "/login" in page.get_url_courante()
```

#### Étape 3 — Lancer les tests

```bash
# Depuis le dossier tp-selenium, environnement virtuel activé

# Lancer tous les tests
pytest tests/ -v

# Résultat attendu :
# tests/test_login.py::TestLogin::test_connexion_valide_redirige_vers_securise  PASSED
# tests/test_login.py::TestLogin::test_connexion_valide_affiche_message_succes  PASSED
# tests/test_login.py::TestLogin::test_mot_de_passe_incorrect_affiche_erreur   PASSED
# tests/test_login.py::TestLogin::test_username_incorrect_affiche_erreur        PASSED
# tests/test_login.py::TestLogin::test_champs_vides_restent_sur_page_login      PASSED
# 5 passed in x.xxs

# Lancer avec rapport HTML
pip install pytest-html
pytest tests/ -v --html=rapport_tests.html --self-contained-html
# → ouvrir rapport_tests.html dans le navigateur
```

---

### 2.6 TP2-B — Checkboxes (interactions et état des éléments)

La page `https://the-internet.herokuapp.com/checkboxes` contient deux cases à cocher. Elle est simple mais représentative d'un problème courant : vérifier l'état d'un élément (coché / non coché) plutôt que son texte.

#### Page Object

```python
# pages/checkboxes_page.py
from selenium.webdriver.common.by import By


class CheckboxesPage:

    URL = "/checkboxes"
    CHECKBOXES = (By.CSS_SELECTOR, "input[type='checkbox']")

    def __init__(self, driver, base_url):
        self.driver   = driver
        self.base_url = base_url

    def open(self):
        self.driver.get(self.base_url + self.URL)
        return self

    def get_checkboxes(self):
        """Retourne la liste des deux éléments checkbox."""
        return self.driver.find_elements(*self.CHECKBOXES)

    def est_cochee(self, index):
        """Retourne True si la checkbox à l'index donné est cochée."""
        return self.get_checkboxes()[index].is_selected()

    def cocher(self, index):
        """Coche la checkbox si elle ne l'est pas déjà."""
        cb = self.get_checkboxes()[index]
        if not cb.is_selected():
            cb.click()
        return self

    def decocher(self, index):
        """Décoche la checkbox si elle est cochée."""
        cb = self.get_checkboxes()[index]
        if cb.is_selected():
            cb.click()
        return self
```

#### Tests

```python
# tests/test_checkboxes.py
import pytest
from pages.checkboxes_page import CheckboxesPage


class TestCheckboxes:

    def test_etat_initial_premiere_checkbox_non_cochee(self, driver, base_url):
        """
        GIVEN la page checkboxes
        WHEN elle est ouverte sans interaction
        THEN la première checkbox est décochée par défaut
        """
        page = CheckboxesPage(driver, base_url).open()
        assert not page.est_cochee(0), "Checkbox 1 devrait être décochée par défaut"

    def test_etat_initial_deuxieme_checkbox_cochee(self, driver, base_url):
        """
        GIVEN la page checkboxes
        WHEN elle est ouverte sans interaction
        THEN la deuxième checkbox est cochée par défaut
        """
        page = CheckboxesPage(driver, base_url).open()
        assert page.est_cochee(1), "Checkbox 2 devrait être cochée par défaut"

    def test_cocher_premiere_checkbox(self, driver, base_url):
        """
        GIVEN la page checkboxes avec checkbox 1 décochée
        WHEN je coche la checkbox 1
        THEN elle est cochée
        """
        # GIVEN
        page = CheckboxesPage(driver, base_url).open()
        assert not page.est_cochee(0)

        # WHEN
        page.cocher(0)

        # THEN
        assert page.est_cochee(0), "Checkbox 1 devrait être cochée après le clic"

    def test_decocher_deuxieme_checkbox(self, driver, base_url):
        """
        GIVEN la page checkboxes avec checkbox 2 cochée
        WHEN je décoche la checkbox 2
        THEN elle est décochée
        """
        # GIVEN
        page = CheckboxesPage(driver, base_url).open()
        assert page.est_cochee(1)

        # WHEN
        page.decocher(1)

        # THEN
        assert not page.est_cochee(1), "Checkbox 2 devrait être décochée après le clic"

    def test_cocher_les_deux_checkboxes(self, driver, base_url):
        """
        GIVEN la page checkboxes
        WHEN je coche les deux checkboxes
        THEN toutes les deux sont cochées
        """
        page = CheckboxesPage(driver, base_url).open()
        page.cocher(0).cocher(1)

        assert page.est_cochee(0), "Checkbox 1 devrait être cochée"
        assert page.est_cochee(1), "Checkbox 2 devrait être cochée"
```

---

### 2.7 Bonnes pratiques pour des tests Selenium maintenables

**1. Toujours utiliser le Page Object Model.** Les locators changent — centralise-les dans les Page Objects, pas dans les tests.

**2. Éviter les `time.sleep()` fixes.** Un `sleep(3)` fige le test même quand la page est prête. Utilise `implicitly_wait` (déjà configuré dans la fixture) ou les `WebDriverWait` explicites pour les cas complexes :

```python
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# Attendre que l'élément soit cliquable (max 10s)
WebDriverWait(driver, 10).until(
    EC.element_to_be_clickable((By.ID, "mon-bouton"))
).click()
```

**3. Un test = un comportement.** Ne mélange pas plusieurs assertions sur des comportements différents dans un même test.

**4. Les tests doivent être indépendants.** Chaque test doit fonctionner seul, dans n'importe quel ordre. C'est pour ça que la fixture `driver` recrée un navigateur à chaque test.

---

## Partie III — TDD et Mocking

### 3.1 Le Test-Driven Development (TDD)

TDD inverse l'ordre habituel : on écrit le test **avant** d'écrire le code. Le cycle se répète en trois étapes :

```
┌─────────┐     ┌────────────┐     ┌───────────┐
│  RED    │────►│   GREEN    │────►│  REFACTOR │
│         │     │            │     │           │
│ Écrire  │     │ Écrire le  │     │ Améliorer │
│ le test │     │ minimum de │     │ le code   │
│ (il     │     │ code pour  │     │ sans      │
│ échoue) │     │ le faire   │     │ casser    │
│         │     │ passer     │     │ les tests │
└─────────┘     └────────────┘     └───────────┘
        ◄────────────────────────────────┘
                 (recommencer)
```

**Pourquoi RED d'abord ?** Un test qui passe sans code écrit est un test qui ne vérifie rien. Le voir échouer prouve qu'il est capable de détecter un bug.

---

### 3.2 TP3-A — Ajouter une méthode en TDD

Tu vas ajouter une nouvelle méthode au `CommandeService` existant : `calculerTVA`. Elle n'existe pas encore dans le code.

**Spécification métier :**
- Calcule la TVA (20%) sur un montant donné
- Retourne le montant arrondi à 2 décimales
- Lève `IllegalArgumentException` si le montant est négatif

#### Cycle 1 — RED : écrire le test avant le code

Ouvre `CommandeServiceTest.java` et ajoute ces tests à la fin de la classe :

```java
// ─────────────────────────────────────────────────
// calculerTVA — TDD : ces tests sont écrits AVANT
// que la méthode existe dans CommandeService
// ─────────────────────────────────────────────────

@Test
@DisplayName("TVA 20% sur 100€ = 20€")
void calculerTVA_CentEuros_RetourneVingt() {
    double tva = service.calculerTVA(100.0);
    assertEquals(20.0, tva, 0.01);
}

@Test
@DisplayName("TVA sur 33.33€ arrondie à 2 décimales")
void calculerTVA_MontantDecimal_RetourneArrondi() {
    double tva = service.calculerTVA(33.33);
    assertEquals(6.67, tva, 0.01);
}

@Test
@DisplayName("TVA sur 0€ = 0€")
void calculerTVA_Zero_RetourneZero() {
    double tva = service.calculerTVA(0.0);
    assertEquals(0.0, tva, 0.01);
}

@Test
@DisplayName("Montant négatif lève une IllegalArgumentException")
void calculerTVA_MontantNegatif_LeveException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.calculerTVA(-10.0)
    );
}
```

**Lance les tests :**

```bash
mvn clean test
```

Résultat attendu :
```
[ERROR] calculerTVA_CentEuros_RetourneVingt — COMPILATION ERROR
# La méthode calculerTVA n'existe pas encore → erreur de compilation
# C'est normal. C'est le RED.
```

#### Cycle 2 — GREEN : écrire le minimum de code pour passer

Ouvre `CommandeService.java` et ajoute la méthode **en faisant le strict minimum** pour que les tests passent :

```java
/**
 * Calcule la TVA à 20% sur un montant donné.
 *
 * @param montant le montant HT
 * @return la TVA arrondie à 2 décimales
 * @throws IllegalArgumentException si le montant est négatif
 */
public double calculerTVA(double montant) {
    if (montant < 0) {
        throw new IllegalArgumentException("Montant négatif : " + montant);
    }
    double tva = montant * 0.20;
    // Math.round multiplie par 100, arrondit, divise par 100
    return Math.round(tva * 100.0) / 100.0;
}
```

**Relance :**

```bash
mvn clean test
```

Résultat attendu :
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
# Tous les tests passent. C'est le GREEN.
```

#### Cycle 3 — REFACTOR : améliorer sans casser

Le code est simple ici, mais entraîne-toi à la discipline : relis `calculerTVA`, demande-toi si le nom est clair, si la Javadoc est complète, si l'exception est bien typée. Relance les tests après chaque modification.

**Ajouter un test de valeur frontière (bonus) :**

```java
@Test
@DisplayName("TVA sur montant très petit (0.01€)")
void calculerTVA_UnCentime_RetourneZero() {
    // 0.01 * 0.20 = 0.002 → arrondi à 0.00
    double tva = service.calculerTVA(0.01);
    assertEquals(0.0, tva, 0.001);
}
```

**Pourquoi ce test ?** Il vérifie le comportement sur un cas limite que la spécification métier n'avait pas explicitement prévu. C'est ce genre de test que TDD encourage : penser aux cas extrêmes pendant qu'on écrit, pas après.

---

### 3.3 Le Mocking avec Mockito

#### Pourquoi mocker ?

Les tests unitaires testent **une classe en isolation**. Si `CommandeService` dépend d'un `StockRepository` (qui lit en base de données), tu ne veux pas que tes tests dépendent d'une vraie base. Tu crées un **mock** : un objet simulé qui remplace la vraie dépendance.

```
Test unitaire SANS mock          Test unitaire AVEC mock
                                 
CommandeService                  CommandeService
       │                                │
       ▼                                ▼
StockRepository ──► BDD         StockRepository (mock)
(lent, fragile,                  (instantané, contrôlé,
 dépend de l'infra)               pas de BDD)
```

#### La dépendance à simuler

Ajoute cette interface dans `src/main/java/fr/epsi/repository/StockRepository.java` :

```java
package fr.epsi.repository;

import fr.epsi.model.Article;

/**
 * Contrat pour la vérification du stock.
 * En production : interroge une base de données.
 * Dans les tests : on le mocke.
 */
public interface StockRepository {
    /**
     * Retourne le stock disponible pour un article donné.
     * @param article l'article recherché
     * @return la quantité en stock (0 si indisponible)
     */
    int getStock(Article article);
}
```

Ajoute une nouvelle méthode dans `CommandeService.java` qui utilise cette dépendance :

```java
// Ajouter en attribut de classe
private StockRepository stockRepository;

// Constructeur sans dépendance (comportement existant inchangé)
public CommandeService() {
    this.stockRepository = null;
}

// Nouveau constructeur avec injection de dépendance
public CommandeService(StockRepository stockRepository) {
    this.stockRepository = stockRepository;
}

/**
 * Vérifie si la commande est réalisable selon le stock disponible.
 *
 * @param article   l'article à commander
 * @param quantite  la quantité demandée
 * @return true si le stock est suffisant
 * @throws IllegalStateException si aucun StockRepository n'est configuré
 */
public boolean commandeRealisable(Article article, int quantite) {
    if (stockRepository == null) {
        throw new IllegalStateException("StockRepository non configuré");
    }
    int stockDisponible = stockRepository.getStock(article);
    return stockDisponible >= quantite;
}
```

---

### 3.4 TP3-B — Écrire les tests avec Mockito

Mockito est déjà dans le `pom.xml` (ajouté lors d'ICDE848). Crée un nouveau fichier de test :

```java
// src/test/java/fr/epsi/service/CommandeServiceMockTest.java
package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires avec Mockito.
 * @Mock crée un objet simulé de StockRepository.
 * On contrôle ce qu'il retourne sans toucher à une vraie base de données.
 */
@ExtendWith(MockitoExtension.class)
class CommandeServiceMockTest {

    @Mock
    private StockRepository stockRepository;   // Mock créé par Mockito

    private CommandeService service;
    private Article article;

    @BeforeEach
    void setUp() {
        // Injecter le mock dans le service
        service = new CommandeService(stockRepository);
        article = new Article("Stylo", 2.0);
    }

    @Test
    @DisplayName("Stock suffisant → commande réalisable")
    void commandeRealisable_StockSuffisant_RetourneTrue() {
        // GIVEN — le mock retourne 10 quand on lui demande le stock de "Stylo"
        when(stockRepository.getStock(article)).thenReturn(10);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "La commande devrait être réalisable (stock=10, demande=5)");

        // Vérifier que le mock a bien été appelé exactement une fois
        verify(stockRepository, times(1)).getStock(article);
    }

    @Test
    @DisplayName("Stock insuffisant → commande non réalisable")
    void commandeRealisable_StockInsuffisant_RetourneFalse() {
        // GIVEN — stock de 2 seulement
        when(stockRepository.getStock(article)).thenReturn(2);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertFalse(resultat, "La commande ne devrait pas être réalisable (stock=2, demande=5)");
    }

    @Test
    @DisplayName("Stock exactement égal à la demande → réalisable")
    void commandeRealisable_StockEgalDemande_RetourneTrue() {
        // GIVEN — stock = quantité demandée exactement (frontière)
        when(stockRepository.getStock(article)).thenReturn(5);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "Commande égale au stock devrait être réalisable");
    }

    @Test
    @DisplayName("Stock à zéro → commande non réalisable")
    void commandeRealisable_StockZero_RetourneFalse() {
        // GIVEN — article en rupture de stock
        when(stockRepository.getStock(article)).thenReturn(0);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 1);

        // THEN
        assertFalse(resultat, "Stock à zéro : commande non réalisable");
    }

    @Test
    @DisplayName("Sans StockRepository → IllegalStateException")
    void commandeRealisable_SansRepository_LeveException() {
        // GIVEN — service créé sans repository
        CommandeService serviceNonConfigure = new CommandeService();

        // WHEN + THEN
        assertThrows(
            IllegalStateException.class,
            () -> serviceNonConfigure.commandeRealisable(article, 1)
        );
    }
}
```

**Lance les tests :**

```bash
mvn clean test
# → 20 tests, 0 échecs (11 existants + 5 TDD + 5 Mock)
```

**Ce que Mockito a fait ici :**
- `@Mock StockRepository` : crée un objet factice qui implémente l'interface
- `when(...).thenReturn(...)` : programme le comportement du mock pour ce test précis
- `verify(..., times(1))` : vérifie que la méthode a bien été appelée — utile pour s'assurer que le service interroge bien le stock

---

## Partie IV — Gestion des données de test et intégration CI

### 4.1 Données de test dynamiques

Tester toujours avec les mêmes valeurs est insuffisant. Pytest offre `@pytest.mark.parametrize` pour lancer le même test avec plusieurs jeux de données.

Ajoute ce test dans `tests/test_login.py` :

```python
import pytest
from pages.login_page import LoginPage

class TestLoginParametrize:
    """
    Même test lancé avec plusieurs combinaisons identifiants/attendu.
    Un seul test couvre quatre scénarios.
    """

    @pytest.mark.parametrize("username, password, message_attendu", [
        # Cas valide
        ("tomsmith", "SuperSecretPassword!", "You logged into a secure area"),
        # Cas invalides
        ("tomsmith",    "mauvaismdp",         "Your password is invalid"),
        ("inconnu",     "SuperSecretPassword!", "Your username is invalid"),
        ("",            "",                    "Your username is invalid"),
    ])
    def test_login_scenarios(self, driver, base_url, username, password, message_attendu):
        """
        GIVEN la page de login
        WHEN je tente de me connecter avec username/password
        THEN le message flash contient message_attendu
        """
        page = LoginPage(driver, base_url)
        page.open()
        page.se_connecter(username, password)

        message = page.get_message_flash()
        assert message_attendu in message, \
            f"[{username}/{password}] Attendu : '{message_attendu}', Obtenu : '{message}'"
```

```bash
pytest tests/test_login.py::TestLoginParametrize -v
# Lance 4 cas de test à partir d'un seul test écrit
```

---

### 4.2 Générer un rapport JUnit XML (pour Jenkins)

Jenkins lit les rapports au format JUnit XML — le même format que Surefire. Pytest peut en générer un :

```bash
pip install pytest-junit
pytest tests/ -v --junitxml=rapport-selenium.xml
```

Ce fichier `rapport-selenium.xml` sera lu par le plugin JUnit de Jenkins.

---

### 4.3 Intégrer les tests Selenium dans le pipeline Jenkins

Le pipeline Jenkins existant (Jenkinsfile) ne couvre que les tests Java. Tu vas y ajouter un stage pour les tests Selenium Python.

#### Prérequis sur le serveur Jenkins

Python 3 et pip doivent être disponibles sur la machine Jenkins :

```bash
# Vérifier sur le serveur Jenkins
python3 --version
pip3 --version

# Installer les dépendances si besoin
pip3 install selenium pytest pytest-html
```

#### Ajouter le stage dans le Jenkinsfile

Ouvre le `Jenkinsfile` du projet `tp-jenkins` et ajoute ce stage **après le stage "Qualité"** et **avant "Archive"** :

```groovy
// ── Stage 7 : Tests UI Selenium ───────────────────────────
stage('Tests UI Selenium') {
    steps {
        // Se placer dans le dossier des tests Selenium
        // (adapter le chemin selon où tu as mis tp-selenium)
        dir('tp-selenium') {
            sh '''
                python3 -m venv venv
                . venv/bin/activate
                pip install -r requirements.txt --quiet
                pytest tests/ \
                    -v \
                    --junitxml=rapport-selenium.xml \
                    --html=rapport-selenium.html \
                    --self-contained-html
            '''
        }
    }
    post {
        always {
            // Publier les résultats dans Jenkins
            junit 'tp-selenium/rapport-selenium.xml'

            // Archiver le rapport HTML
            archiveArtifacts(
                artifacts: 'tp-selenium/rapport-selenium.html',
                allowEmptyArchive: true
            )
        }
        failure {
            echo 'Tests UI en échec — consulter rapport-selenium.html'
        }
    }
}
```

> **Note pratique.** Si le dossier `tp-selenium` est un dépôt Git séparé, adapte le chemin ou utilise `checkout` pour le récupérer. Pour ce TP, place `tp-selenium/` à la racine du dépôt `tp-jenkins` et ajoute-le au `.gitignore` ou commit-le selon le choix de ton équipe.

---

### 4.4 Organisation finale du pipeline

Après cette intégration, le pipeline complet est :

```
Checkout
   ↓
Build (mvn compile)
   ↓
Tests unitaires (Surefire → JUnit rapport)
   ↓
Tests d'intégration (Failsafe → JUnit rapport)
   ↓
Couverture JaCoCo
   ↓
Qualité (Checkstyle + PMD + CPD + SpotBugs)
   ↓
Tests UI Selenium (pytest → JUnit rapport)
   ↓
Archive (JAR + rapport HTML Selenium)
```

Chaque commit déclenche l'ensemble. Jenkins publie **tous** les résultats dans le tableau de bord.

---

### 4.5 Stratégies avancées — aperçu

#### Tests parallèles

Pytest-xdist permet de lancer les tests en parallèle — utile quand la suite Selenium devient longue :

```bash
pip install pytest-xdist
pytest tests/ -n 4   # 4 workers en parallèle
```

Chaque worker crée son propre driver Chrome — les tests doivent être **indépendants** (pas d'état partagé entre tests). C'est pourquoi la fixture `driver` recrée un navigateur à chaque test.

#### Cross-browser

Pour tester sur Firefox en plus de Chrome :

```python
# conftest.py — version multi-navigateur
import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.firefox.options import Options as FirefoxOptions

@pytest.fixture(params=["chrome", "firefox"])
def driver(request):
    if request.param == "chrome":
        options = ChromeOptions()
        options.add_argument("--headless")
        driver = webdriver.Chrome(options=options)
    else:
        options = FirefoxOptions()
        options.add_argument("--headless")
        driver = webdriver.Firefox(options=options)

    driver.implicitly_wait(5)
    yield driver
    driver.quit()
```

Chaque test est alors exécuté deux fois — une fois sur Chrome, une fois sur Firefox.

#### TDD et couverture — le lien

Avec TDD, la couverture JaCoCo tend naturellement vers 100% sur le code nouveau : chaque ligne est écrite pour faire passer un test qui l'exige. Lance `mvn clean verify` après avoir ajouté `calculerTVA` et `commandeRealisable` — observe l'évolution du rapport JaCoCo.

---

## Étude de cas — Mise en situation professionnelle (MSPR)

### Contexte

TechRetail SA veut étendre la couverture de test de la boutique en ligne. Tu es chargé de mettre en place l'automatisation des tests UI et d'appliquer TDD sur deux nouvelles fonctionnalités.

### Livrables attendus

**1 — Suite de tests Selenium**
Minimum 6 tests UI sur `the-internet.herokuapp.com` couvrant au moins deux pages différentes (login + une autre au choix). Pattern Page Object Model respecté. Rapport HTML et rapport JUnit XML générés. Les tests doivent passer en mode headless.

**2 — Développement en TDD**
Deux méthodes nouvelles ajoutées au `CommandeService` en suivant le cycle Red/Green/Refactor documenté. Pour chaque méthode : capture du terminal en RED (erreur de compilation ou test échoué), capture en GREEN (tous les tests passent).

**3 — Tests avec Mockito**
Minimum 4 tests unitaires utilisant un mock. Le mock doit être vérifié avec `verify()` dans au moins un test.

**4 — Intégration CI**
Le stage "Tests UI Selenium" présent dans le Jenkinsfile et fonctionnel dans Jenkins. Capture du tableau de bord Jenkins montrant les résultats des tests Java ET des tests Selenium sur le même build.

**5 — Rapport écrit (1 à 2 pages)**
Description d'une violation détectée grâce aux tests (bug trouvé par un test qui échoue). Justification du choix des cas de test pour la suite Selenium (quels scénarios, pourquoi ces valeurs). Analyse de la couverture JaCoCo avant/après TDD.

### Grille d'évaluation

| Critère | Points |
|---------|--------|
| Suite Selenium : POM, ≥ 6 tests, 2 pages, headless | 5 |
| TDD : cycle documenté, 2 méthodes, captures RED/GREEN | 4 |
| Mocking : ≥ 4 tests, verify() présent | 3 |
| Intégration CI : stage Jenkins fonctionnel, captures | 4 |
| Rapport : bug trouvé, justification choix tests, JaCoCo | 4 |
| **Total** | **20** |

---

## Ressources

- Selenium documentation officielle : https://www.selenium.dev/documentation/
- The Internet (application de démo) : https://the-internet.herokuapp.com
- Pytest documentation : https://docs.pytest.org/
- Pytest-html (rapports) : https://pytest-html.readthedocs.io/
- Mockito documentation : https://site.mockito.org/
- Awesome Test Automation (liste d'outils) : https://github.com/atinfo/awesome-test-automation
- Selenium - Maîtrisez vos tests fonctionnels avec Python — Bibliothèque ENI
- 360 Learning, LinkedIn Learning, Bibliothèque ENI

---

*TESE860 · Atelier : Automatisation des tests · EPSI · Mastère EISI 1ère année · 2025-2026*
