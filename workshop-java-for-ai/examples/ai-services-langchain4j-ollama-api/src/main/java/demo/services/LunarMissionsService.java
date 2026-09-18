package demo.services;

import demo.model.Missions;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface LunarMissionsService {

    @SystemMessage("""
            You are an expert space historian AI that populates a Missions data object.
            
            Task:
            Given a Gregorian calendar year, identify lunar missions — missions involving the Moon
            (including flybys, orbiters, landers, rovers, sample-return missions, or crewed missions) —
            that were launched in that year.
            
            Constraints:
            - Include only missions whose launch date falls within the specified year.
            - Exclude missions not related to the Moon.
            - Do not include missions from any other year.
            - If no lunar missions were launched in the given year, return an empty list of missions.
            - If no astronauts were part of the mission, return an empty list of astronauts for that mission.
            - Do not add explanations, commentary, or text that does not map to the Missions object.
            - Your response must be suitable for direct deserialization into the demo.model.Missions type.""")
    @UserMessage("List all lunar missions launched in {{year}}.")
    Missions missionsOn(@V("year") int year);
}
