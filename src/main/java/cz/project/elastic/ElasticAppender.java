package cz.project.elastic;

import cz.project.Const;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Appender pro Log4j
 * zapisujice zaznamy do elasticu, v pripade vypadku zapisuje do souboru
 */
public class ElasticAppender extends AppenderSkeleton {
    private static final String backupLogDir = "c:/logs";

    private TransportClient client;
    private BufferedWriter backupLogWriter;

    public ElasticAppender() {
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void append(LoggingEvent event) {
        IndexResponse indexResponse;

        try {
            // try send
            indexResponse = client.prepareIndex(Const.ELASTIC_LOG_INDEX_NAME, "log").setSource(recordToJson(event), XContentType.JSON).get();
        } catch (NoNodeAvailableException ex) {
            // send not success
            indexResponse = null;
        }

        if (indexResponse == null || indexResponse.status() != RestStatus.CREATED) {
            // not success or not created -> write to backup file
            writeToBackupLogFile(recordToString(event));
        }
    }

    private void initFileWriter() {
        try {
            File backupLogFile = new File(backupLogDir + "/" + System.currentTimeMillis() + ".log");
            if (backupLogFile.getParentFile() != null) {
                // create parent dirs
                backupLogFile.getParentFile().mkdirs();
            }
            if (!backupLogFile.exists()) {
                // create log file
                backupLogFile.createNewFile();
            }
            backupLogWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupLogFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToBackupLogFile(String record) {
        if (backupLogWriter == null) {
            // create path & file and connect
            initFileWriter();
        }

        try {
            backupLogWriter.write(record);
            backupLogWriter.newLine();
            backupLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String recordToJson(LoggingEvent event) {
        return new JSONObject()
                .put("level", "[" + event.getLevel().toString() + "]")
                .put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(event.getTimeStamp())))
                .put("location", event.getLocationInformation().getClassName())
                .put("message", event.getMessage())
                .toString();
    }

    private String recordToString(LoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(event.getLevel().toString()).append("] ");
        sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(event.getTimeStamp())));
        sb.append(" ").append(event.getLocationInformation().getClassName()).append(" - ");
        sb.append(event.getMessage());
        return sb.toString();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
        if (backupLogWriter != null) {
            try {
                backupLogWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
