@comment
@disable
Feature: comment Constitution

  Scenario: Can comment an constitution
    Given I have citizen Nicolas Copernic
    And I am authenticated as Nicolas Copernic
    And I have constitution
      | id | 1707c287-a472-4a62-89f2-9e85030e915c |
    When I send a POST request to "/constitutions/1707c287-a472-4a62-89f2-9e85030e915c/comments" with body:
    """
    {
      "content": "Hello mister"
    }
    """
    Then the response status code should be 201

  Scenario: Can get comments on constitutions of the current citizen
    Given I have citizen Charles Darwin with ID "46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5"
    And I have constitution
      | id        | 34ddd50a-da00-4a90-a869-08baa2a121be |
      | createdBy | Charles Darwin                       |
    And I have comment created by Charles Darwin on constitution "34ddd50a-da00-4a90-a869-08baa2a121be"
    When I send a GET request to "/citizens/46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5/comments/constitutions"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |
    And the Response should contain:
    """
    34ddd50a-da00-4a90-a869-08baa2a121be
    """
