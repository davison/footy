package org.davisononline.footy.match

/*
 * filters footy-core controller calls to enhance the model for footy-match
 * templates
 */
class ModelEnhancingFilters {

    def footyMatchService


    def filters = {
        teamShow (controller:'team', action:'show') { 

            after = { model ->
                // model will not exist if team was not found..
                if (model) {
                    model['fixtures'] = footyMatchService.getFixtures(model['teamInstance'])
                    model['totalFixtureCount'] = Fixture.countByTeam(model['teamInstance'])
                    model['myteam'] = model['teamInstance']
                }
            }
            
        }
    }
    
}