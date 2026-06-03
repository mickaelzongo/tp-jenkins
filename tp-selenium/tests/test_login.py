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