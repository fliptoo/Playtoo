package controllers.Playtoo;

import models.Playtoo.Check;
import models.Playtoo.Secured;
import models.Playtoo.db.UserID;

@Secured
@Check(UserID.ADMIN)
public class BaseOffice extends BaseWeb {

}
