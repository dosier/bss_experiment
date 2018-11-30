import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

/**
 * TODO: add documentation
 *
 * @author Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @version 1.0
 * @since 2018-11-29
 */
public class ExperimentScreen extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        final HashMap<WordListCategory, WordList[]> wordListCategoryHashMap = new HashMap<>();

        wordListCategoryHashMap.put(WordListCategory.EASY_TO_READ_WORDS, WordList.Companion.getEASY_LISTS());
        wordListCategoryHashMap.put(WordListCategory.HARD_TO_READ_WORDS, WordList.Companion.getHARD_LISTS());

        final ExperimentSession sceneBuilder = new ExperimentSession(WordListCategory.EASY_TO_READ_WORDS, wordListCategoryHashMap);
        final Scene scene = sceneBuilder.buildScene();
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("BSS Experiment");
        primaryStage.setScene(scene);
        primaryStage.show(); // wait till the user entered an answer
    }
}
