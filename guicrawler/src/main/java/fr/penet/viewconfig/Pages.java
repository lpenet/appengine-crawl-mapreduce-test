package fr.penet.viewconfig;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.REDIRECT;

@Folder(name = "/")
@View(navigation = REDIRECT)
public interface Pages {
    class Accueil implements ViewConfig { }
    class RunPages implements ViewConfig { }
}
