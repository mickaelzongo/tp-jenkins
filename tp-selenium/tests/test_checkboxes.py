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