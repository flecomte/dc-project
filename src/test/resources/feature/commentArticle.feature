@comment
@disable
Feature: comment Article

  Scenario: Can comment an article
    Given I have citizen Michael Faraday
    And I am authenticated as Michael Faraday
    And I have article
      | id | aa16c635-28da-46f0-9a89-934eef88c7ca |
    When I send a POST request to "/articles/aa16c635-28da-46f0-9a89-934eef88c7ca/comments" with body:
    """
    {
      "content": "Hello mister"
    }
    """
    Then the response status code should be 201

  # TODO add more comment on article
  Scenario: Can get all comment on article
    Given I have citizen Enrico Fermi
    And I am authenticated as Enrico Fermi
    And I have article
      | id | 6166c078-ca97-4366-b0aa-2a5cd558c78a |
    And I have comment created by Enrico Fermi on article "6166c078-ca97-4366-b0aa-2a5cd558c78a"
    When I send a GET request to "/articles/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments"
    Then the response status code should be 200

  # TODO add votes
  Scenario: Can get all comment on article sorted by votes
    Given I have citizen Pierre Curie
    And I am authenticated as Pierre Curie
    And I have article
      | id | 5e209f63-57ce-43ca-922a-273b0d62f567 |
    And I have comment created by Pierre Curie on article "5e209f63-57ce-43ca-922a-273b0d62f567"
    When I send a GET request to "/articles/5e209f63-57ce-43ca-922a-273b0d62f567/comments?sort=votes"
    Then the response status code should be 200
    And the response should contain object:
      | $.result[0].votes.up | 0 |

  Scenario: Can get comments on articles of the current citizen
    Given I have citizen Erwin Schrodinger with ID "292a20cc-4a60-489e-9866-a95d38ffaf47"
    And I have article
      | id        | 17df7fb9-b388-4e20-ab19-29c29972da01 |
      | createdBy | Erwin Schrodinger                    |
    And I have comment created by Erwin Schrodinger on article "17df7fb9-b388-4e20-ab19-29c29972da01"
    When I send a GET request to "/citizens/292a20cc-4a60-489e-9866-a95d38ffaf47/comments/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |
    And the Response should contain:
    """
    292a20cc-4a60-489e-9866-a95d38ffaf47
    """

  Scenario: Can edit a comment
    Given I have citizen Hubert Reeves
    And I have article
      | id | bb05e4a3-55a1-4088-85e7-8d8c23be29b1 |
    And I am authenticated as Hubert Reeves
    And I have comment created by Hubert Reeves on article "bb05e4a3-55a1-4088-85e7-8d8c23be29b1":
      | id | fd30d20f-656c-42c6-8955-f61c04537464 |
    When I send a PUT request to "/comments/fd30d20f-656c-42c6-8955-f61c04537464" with body:
    """
    Hello boy
    """
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |

  Scenario: Can get comment by its ID
    Given I have citizen Alfred Kastler
    And I have article
      | id | 3897465b-19d2-43a0-86ea-1e29dbb11ec9 |
    And I have comment created by Alfred Kastler on article "3897465b-19d2-43a0-86ea-1e29dbb11ec9":
      | id      | edd296a8-fc7a-4717-a2bb-9f035ceca3c2 |
      | content | Hello boy                            |
    When I send a GET request to "/comments/edd296a8-fc7a-4717-a2bb-9f035ceca3c2"
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |
