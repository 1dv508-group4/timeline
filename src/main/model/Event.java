package main.model;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

//import static main.controller.NewTimelineFragment.myTime;

@XmlRootElement(name="Event")
@XmlType(propOrder = {"event_title", "event_description", "event_startDate", "event_endDate", "timeline_id","event_id","durational", "level"})
@XmlAccessorType(XmlAccessType.FIELD)

public class Event implements Comparable {
	@XmlElement (name = "Durational")
    private boolean durational;
	@XmlElement(name = "Title")
	private String event_title;
	@XmlElement(name = "Description")
	private String event_description;
	@XmlJavaTypeAdapter(value = LocalDatetoXMLAdapter.class)
	@XmlElement(name = "StartDate")
	private LocalDate event_startDate;
	@XmlJavaTypeAdapter(value = LocalDatetoXMLAdapter.class)
	@XmlElement(name = "EndDate")
	private LocalDate event_endDate;
	@XmlElement(name = "TimeLineId")
	private int timeline_id;
	@XmlElement(name = "EventID")
	private int event_id;
	@XmlElement(name = "Level")
	private int level = 0;


    public Event(){}

    public Event(String eventName, String eventDescription, LocalDate start, LocalDate end) {
        this.event_title = eventName;
        this.event_description = eventDescription;
        this.event_startDate = start;
        this.event_endDate = end;
        durational = true;
    }

    public Event(String eventName, String eventDescription, LocalDate date) {
        this.event_title = eventName;
        this.event_description = eventDescription;
        this.event_startDate = date;
        durational = false;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getEvent_description() {
        return event_description;
    }

    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public LocalDate getEvent_startDate() {
        return event_startDate;
    }

    public void setEvent_startDate(LocalDate event_startDate) {
        this.event_startDate = event_startDate;
    }

    public LocalDate getEvent_endDate() {
        return event_endDate;
    }

    public void setEvent_endDate(LocalDate event_endDate) {
        this.event_endDate = event_endDate;
    }

    public int getTimeline_id() {
        return timeline_id;
    }

    public void setTimeline_id(int timeline_id) {
        this.timeline_id = timeline_id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setDurational(boolean durational) {
        this.durational = durational;
    }

    public boolean isDurational() {
        return durational;
    }

    public String toString(){
        return this.getEvent_title()+","+this.getEvent_startDate()+","+this.getEvent_endDate()+","+this.getEvent_description();
    }

    @Override
    public int compareTo(Object o) {
        Event event = (Event) o;
        if (this.isDurational() && event.isDurational())
            return 0;
        else if (this.isDurational() && !event.isDurational()) {
            return -1;
        } else
            return 1;
    }
}
