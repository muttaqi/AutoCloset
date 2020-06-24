package com.autocloset.model;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Random;

/**
 * This code example is featured in this youtube video
 * https://www.youtube.com/watch?v=zrTSs715Ylo
 *
 * This differs slightly from the Video Example,
 * The Video example had the data already downloaded
 * This example includes code that downloads the data
 *
 * Data is downloaded from
 * wget http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz
 * followed by tar xzvf mnist_png.tar.gz
 *
 * This examples builds on the MnistImagePipelineExample
 * by Loading the previously saved Neural Net
 */
public class AutoClosetModelTrain {
    private static Logger log = LoggerFactory.getLogger(AutoClosetModelTrain.class);

    /** Data URL for downloading */
    public static final String DATA_URL = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz";

    /** Location to save and extract the training/testing data */
    public static final String DATA_PATH = "C:/Users/Home-PC_2/dl4j-examples - Copy/dl4j-examples/src/main/resources/clothes";

    public static void main(String[] args) throws Exception {
        // image information
        // 28 * 28 grayscale
        // grayscale implies single channel
        int height = 100;
        int width = 100;
        int channels = 1;
        int rngseed = 123;
        Random randNumGen = new Random(rngseed);
        int batchSize = 10;
        int outputNum = 4;
        int numEpochs = 50;

    /*
    This class downloadData() downloads the data
    stores the data in java's tmpdir 15MB download compressed
    It will take 158MB of space when uncompressed
    The data can be downloaded manually here
    http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz
    */

        // Define the File Paths
        File trainData = new File(DATA_PATH + "/train-data");
        File testData = new File(DATA_PATH + "/test-data");

        // Define the FileSplit(PATH, ALLOWED FORMATS,random)
        FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);

        // Extract the parent path as the image label
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

        ImageRecordReader trainRecordReader = new ImageRecordReader(height, width, channels, labelMaker);
        ImageRecordReader testRecordReader = new ImageRecordReader(height, width, channels, labelMaker);

        // Initialize the record reader
        // add a listener, to extract the name
        trainRecordReader.initialize(train);
        //recordReader.setListeners(new LogRecordListener());

        // DataSet Iterator
        DataSetIterator trainDataIter = new RecordReaderDataSetIterator(trainRecordReader, batchSize, 1, outputNum);

        // Scale pixel values to 0-1
        DataNormalization trainScaler = new ImagePreProcessingScaler(0, 1);
        trainScaler.fit(trainDataIter);
        trainDataIter.setPreProcessor(trainScaler);

        //Set up model testing
        testRecordReader.initialize(test);

        DataSetIterator testDataIter = new RecordReaderDataSetIterator(testRecordReader, batchSize, 1, outputNum);

        DataNormalization testScaler = new ImagePreProcessingScaler(0, 1);
        testScaler.fit(testDataIter);
        testDataIter.setPreProcessor(testScaler);

        // Build Our Neural Network
        log.info("LOAD TRAINED MODEL");
        // Where the saved model would be if
        // MnistImagePipelineSave has been run
        File locationToSave = new File(DATA_PATH + "\\trained_clothing_model.zip");

        if (locationToSave.exists()) {
            log.info("Saved Model Found!");
        } else {
            log.error("File not found!");

            log.info(locationToSave.getAbsolutePath());
            log.error("This example depends on running MnistImagePipelineExampleSave, run that example first");
            System.exit(0);
        }

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        model.getLabels();

        model.setListeners(new ScoreIterationListener(20));

        trainModel(numEpochs, model, trainDataIter);

        // Test the Loaded Model with the test data

        testModel(testDataIter, outputNum, model);
    }

    public static void trainModel(int numEpochs, MultiLayerNetwork model, DataSetIterator dataIter) throws IOException{

        log.info("TRAIN MODEL");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(dataIter);
        }

        log.info("SAVE TRAINED MODEL");
        // Where to save model
        File locationToSave = new File(DATA_PATH + "/trained_clothing_model.zip");

        // boolean save Updater
        boolean saveUpdater = false;

        // ModelSerializer needs modelname, saveUpdater, Location
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);
    }

    public static void testModel(DataSetIterator testIter, int outputNum, MultiLayerNetwork model) {

        // Create Eval object with 4 possible classes
        Evaluation eval = new Evaluation(outputNum);

        while (testIter.hasNext()) {
            DataSet next = testIter.next();
            INDArray output = model.output(next.getFeatures());
            eval.eval(next.getLabels(), output);
        }

        log.info(eval.stats());
    }
}
