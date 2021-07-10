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

// module for re-loading the model and performing further training as well as testing
public class AutoClosetModelTrain {
    private static Logger log = LoggerFactory.getLogger(AutoClosetModelTrain.class);

    // location to save and extract the training/testing data
    public static final String DATA_PATH = "../clothing";

    public static void main(String[] args) throws Exception {
        // image information
        // 100 * 100 grayscale; single channel
        int height = 100;
        int width = 100;
        int channels = 1;
        int rngseed = 123;
        Random randNumGen = new Random(rngseed);
        int batchSize = 10;
        int outputNum = 4;
        int numEpochs = 50;

        // file paths
        File trainData = new File(DATA_PATH + "/train-data");
        File testData = new File(DATA_PATH + "/test-data");

        // define the FileSplit
        FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);

        // Extract the parent path as the image label
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

        ImageRecordReader trainRecordReader = new ImageRecordReader(height, width, channels, labelMaker);
        ImageRecordReader testRecordReader = new ImageRecordReader(height, width, channels, labelMaker);

        // initialize the record reader
        trainRecordReader.initialize(train);

        // DataSet Iterator
        DataSetIterator trainDataIter = new RecordReaderDataSetIterator(trainRecordReader, batchSize, 1, outputNum);

        // scale pixel values to 0-1
        DataNormalization trainScaler = new ImagePreProcessingScaler(0, 1);
        trainScaler.fit(trainDataIter);
        trainDataIter.setPreProcessor(trainScaler);

        //set up model testing
        testRecordReader.initialize(test);

        DataSetIterator testDataIter = new RecordReaderDataSetIterator(testRecordReader, batchSize, 1, outputNum);

        DataNormalization testScaler = new ImagePreProcessingScaler(0, 1);
        testScaler.fit(testDataIter);
        testDataIter.setPreProcessor(testScaler);

        // load our neural network
        log.info("LOAD TRAINED MODEL");
        
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

        // train the model
        trainModel(numEpochs, model, trainDataIter);

        // test the loaded model with the test data
        testModel(testDataIter, outputNum, model);
    }

    public static void trainModel(int numEpochs, MultiLayerNetwork model, DataSetIterator dataIter) throws IOException{

        log.info("TRAIN MODEL");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(dataIter);
        }

        log.info("SAVE TRAINED MODEL");
        // update the model
        File locationToSave = new File(DATA_PATH + "/trained_clothing_model.zip");

        boolean saveUpdater = false;
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);
    }

    public static void testModel(DataSetIterator testIter, int outputNum, MultiLayerNetwork model) {

        // create Eval object with 4 possible classes
        Evaluation eval = new Evaluation(outputNum);

        // get the label of the test data and compare with output of model
        while (testIter.hasNext()) {
            DataSet next = testIter.next();
            INDArray output = model.output(next.getFeatures());
            eval.eval(next.getLabels(), output);
        }

        log.info(eval.stats());
    }
}
