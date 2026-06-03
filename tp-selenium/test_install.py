from selenium import webdriver
from selenium.webdriver.chrome.options import Options

options = Options()
options.add_argument("--headless")   # pas de fenêtre graphique

driver = webdriver.Chrome(options=options)
driver.get("https://www.google.com")
print("Titre :", driver.title)
driver.quit()