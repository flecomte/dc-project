Feature: comment Constitution

  Scenario: Can comment an constitution
    Given I am authenticated as John Toe with id "a6eb1f5a-8c02-42f4-8e8e-a722f26841ef"
    And I have constitution with id "d7e20f0b-3fdd-4638-817a-bbd87054eb82" created by "john-toe"
    When I send a POST request to "/constitutions/d7e20f0b-3fdd-4638-817a-bbd87054eb82/comments" with body:
    """
    Hello mister
    """
    Then the response status code should be 201

  Scenario: Can get comments on constitution of the current citizen
    Given I have citizen John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have constitution with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/comments/constitutions"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: Can edit a comment
    Given I am authenticated as username 3 with id "92877af7-0a45-fd6a-2ed7-fe81e1236b78"
    When I send a PUT request to "/comments/b0422e48-687f-bea7-b45f-b6b301246e97" with body:
    """
    Hello boy
    """
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |

  Scenario: Can get comment by its ID
    When I send a GET request to "/comments/b0422e48-687f-bea7-b45f-b6b301246e97"
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |
