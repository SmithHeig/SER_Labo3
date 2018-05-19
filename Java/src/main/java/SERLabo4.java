import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.filter.Filters;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SERLabo4 {

    /**
     * permet de lire un fichier xml et de le transformer en document
     *
     * @param fileName
     * @return
     */
    private static Document getDOMParsedDocument(final String fileName)
    {
        Document document = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            org.w3c.dom.Document w3cDocument = documentBuilder.parse(fileName);
            document = new DOMBuilder().build(w3cDocument);
        }
        catch (IOException | org.xml.sax.SAXException | ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        return document;
    }

    private static void createXML(Document documentOld){
        try {
            /** STRUCTURE PERMETTANT LES CONTORLES **/
            HashSet<String> filmAlreadySaved = new HashSet<>();

            Document document = new Document();
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            /** Ajout de la dtd à l'xml **/
            DocType docType= new DocType("plex", "projections.dtd"); // A vérifié le premier param
            document.addContent(docType); // Ajout de la dtd au document (référence en haut du document)

            /** Ajout des référence (en tête) sur la feuille xsl **/
            ProcessingInstruction processingInstruction = new ProcessingInstruction("xml-stylesheet");
            HashMap<String,String> procInstrAttr = new HashMap<String,String>();
            procInstrAttr.put("type", "text/xsl");
            procInstrAttr.put("href", "projection.xsl");
            processingInstruction.setData(procInstrAttr);
            document.addContent(processingInstruction);

            Element root = new Element("plex"); // Element de base du nouveau XML

            Element projections = new Element("projections"); // Element qui contiendra les projections (meme chose que labo2)
            Element films = new Element("films"); // Element qui contiendra les films
            Element acteurs = new Element("acteurs");
            Element liste_langages = new Element("liste_langages");
            Element liste_genres = new Element("liste_genres");
            Element liste_mots_cles = new Element("liste_mots_cles");


            /** parcours de toutes les projections **/
            for(Element projectionOld : documentOld.getRootElement().getChildren("projection")){
                Element projection = new Element("projection");

                /** SALLE **/
                Element salle = new Element("salle");
                Element oldSalle = projectionOld.getChild("salle");
                salle.setAttribute("taille", oldSalle.getChild("taille").getValue()); // récupération de la taille de la salle
                salle.setText(oldSalle.getChild("noSalle").getValue());

                projection.addContent(salle);

                /** DATE **/
                Element dateHeure = new Element("date_heure");
                dateHeure.setAttribute("format", "dd.MM.YYYY HH:mm"); // A voir si on peut ne pas mettre en dur
                dateHeure.setText(getDate(projectionOld.getChild("date")));

                projection.addContent(dateHeure);

                /** FILM ID LINK WITH PROJECTION **/
                projection.setAttribute("film_id", projectionOld.getChild("film").getChild("id").getValue());
                projection.setAttribute("titre", projectionOld.getChild("film").getChild("titre").getValue());


                /** FILMS **/
                Element oldFilm = projectionOld.getChild("film");

                /* Check si le film à déjà été enregistré */
                if(!filmAlreadySaved.contains(oldFilm.getChild("id").getValue())){
                    Element film = new Element("film");
                    film.setAttribute("no", oldFilm.getChild("id").getValue()); // id -> no

                    /** Titre **/
                    Element titre = new Element("titre");
                    titre.setText(oldFilm.getChild("titre").getValue());
                    film.addContent(titre);

                    /** DUREE **/
                    Element duree = new Element("duree");
                    duree.setAttribute("format", "minutes");
                    duree.setText(oldFilm.getChild("durée").getValue());
                    film.addContent(duree);

                    /**SYNOPSIS **/
                    Element synopsys = new Element("synopsys"); // Changement entre labo2 et 3
                    synopsys.setText(oldFilm.getChild("synopsis").getValue());
                    film.addContent(synopsys);

                    /** PHOTO **/
                    System.out.println();
                    if(oldFilm.getChild("photo") != null){
                        Element photo = new Element("photo");
                        photo.setAttribute("url", oldFilm.getChild("photo").getAttribute("url").getValue());
                        film.addContent(photo);
                    }

                    /** CRITIQUES **/
                    Element critiques = new Element("critiques");
                    for(Element critiqueOld : oldFilm.getChild("critiques").getChildren("critique")){
                        Element critique = new Element("critique");
                        critique.setAttribute("note", critiqueOld.getAttribute("note").getValue());
                        critique.setText(critiqueOld.getValue());
                        critiques.addContent(critique);
                    }
                    film.addContent(critiques);

                    /** LANGUAGE **/
                    Element langages = new Element("langages");
                    String string_langage = "";
                    if(oldFilm.getChildren("langues") != null) {
                        for (Element langage : oldFilm.getChild("langues").getChildren("langue")) {
                            string_langage += langage.getAttributeValue("no") + " ";
                        }
                        langages.setAttribute("liste", string_langage);
                        film.addContent(langages);
                    }

                    /** GENRES **/
                    Element genres = new Element("genres");
                    String string_genre = "";
                    for(Element genre: oldFilm.getChild("genres").getChildren("genre")){
                        string_genre += genre.getAttributeValue("no") + " ";
                    }
                    genres.setAttribute("liste", string_genre);
                    film.addContent(genres);

                    /** MOT_CLE **/
                    Element motsCles = new Element("mots_cles");
                    String string_motCle = "";
                    for(Element motCle : oldFilm.getChild("mots-cles").getChildren("mot-cle")){
                        string_motCle += motCle.getAttributeValue("no") + " ";
                    }
                    motsCles.setAttribute("liste", string_motCle);
                    film.addContent(motsCles);

                    /** ROLE **/
                    Element roles = new Element("roles");
                    for(Element roleOld: oldFilm.getChild("roles").getChildren("role")){
                        Element role = new Element("role");
                        role.setAttribute("place", roleOld.getChild("place").getValue());
                        role.setAttribute("personnage", roleOld.getChild("personnage").getValue());
                        role.setAttribute("acteur_id", roleOld.getChild("id").getValue());
                        roles.addContent(role);
                    }
                    film.addContent(roles);

                    /** Ajout du film au films **/
                    films.addContent(film); // ajout du film au list de films

                    /** Ajout dans la liste des films traité **/
                    filmAlreadySaved.add(oldFilm.getChild("id").getValue());
                }
                projections.addContent(projection);
            } // END FOR PROJECTION



            /** LIST_LANGUAGES **/
            XPathFactory xpFactory = XPathFactory.instance();

            XPathExpression xp = xpFactory.compile("//film/langages/langage", Filters.element());
            List<Element> langages = (List<Element>)(xp.evaluate(documentOld));
            for(Element el : langages){
                Element langage = new Element("langage");
                langage.addContent(el.getValue());
                langage.setAttribute("no", el.getAttributeValue("no"));

                liste_langages.addContent(langage);
            }

            /** LIST_GENRE **/
            xp = xpFactory.compile("//film/genres/genre", Filters.element());
            List<Element> genres = (List<Element>)(xp.evaluate(documentOld));
            for(Element el: genres){
                Element genre = new Element("genre");
                genre.addContent(el.getValue());
                genre.setAttribute("no", el.getAttributeValue("no"));

                liste_genres.addContent(genre);
            }

            /** LIST_MOTS_CLE **/
            xp = xpFactory.compile("//film/mots-cles/mot-cle");
            List<Element> mots_cles = (List<Element>)(xp.evaluate(documentOld));
            for(Element el: mots_cles){
                Element mot_cle = new Element("mot_cle");
                mot_cle.addContent(el.getValue());
                mot_cle.setAttribute("no", el.getAttributeValue("no"));

                liste_mots_cles.addContent(mot_cle);
            }


            root.addContent(projections); // ajout des projections
            root.addContent(films);
            root.addContent(acteurs);
            root.addContent(liste_langages);
            root.addContent(liste_genres);
            root.addContent(liste_mots_cles);

            document.addContent(root);
            outputter.output(document, new FileOutputStream("projections.xml"));

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getDate(Element dateOld){
        // dd.mm.aaaa
        String date = dateOld.getChild("jour").getValue() + "." + dateOld.getChild("mois").getValue() + "." + dateOld.getChild("annee").getValue();
        date += " " + dateOld.getChild("heure") + ":" + dateOld.getChild("minute");
        return date;
    }

    public static void main(String[] argv){
        Document document = getDOMParsedDocument("cinema.xml");

        Element root = document.getRootElement();
        System.out.println("Root Element :: " + root.getName());

        createXML(document);
    }
}
