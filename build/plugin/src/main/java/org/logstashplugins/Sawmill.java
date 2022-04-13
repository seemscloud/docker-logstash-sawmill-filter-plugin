package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.EventFactory;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.File;

import io.logz.sawmill.Doc;
import io.logz.sawmill.ExecutionResult;
import io.logz.sawmill.Pipeline;
import io.logz.sawmill.PipelineExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@LogstashPlugin(name = "sawmill")
public class Sawmill implements Filter {
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");

    private String id;
    private String sourceField;

    public Sawmill(String id, Configuration config, Context context) {
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        try {
            File file = new File("/root/config/pipelines/sawmill/fragment.json");
            String content = FileUtils.readFileToString(file, "UTF-8");

            Pipeline pipeline = new Pipeline.Factory().create(content);

            for (Event e : events) {
                Doc doc = new Doc(e.toMap());
                ExecutionResult executionResult = new PipelineExecutor().execute(pipeline, doc);

                Map<String, Object> map = doc.getSource();

                Event tmp = new org.logstash.Event(map);
                matchListener.filterMatched(tmp);

                e.overwrite(tmp);
            }
        }
        catch (Exception ex) {
            System.out.print(ex);
        }

        return events;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
