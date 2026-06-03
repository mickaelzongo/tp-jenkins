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