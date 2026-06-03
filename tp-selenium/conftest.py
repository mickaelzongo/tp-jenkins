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