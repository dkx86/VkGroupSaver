package vkgs.sns;

import com.google.gson.Gson;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQuery;
import com.vk.api.sdk.queries.wall.WallGetQueryWithExtended;
import vkgs.Settings;
import org.apache.log4j.Logger;

import java.util.List;


public class Vk {
    private static final int LIMIT = 100;
    private final VkApiClient vk;
    private final UserActor userActor;
    private final Gson gson;
    private final Logger logger;

    public Vk(Logger logger) {
        this.logger = logger;
        vk = new VkApiClient(HttpTransportClient.getInstance());
        userActor = new UserActor(Settings.it().getVkUserId(), Settings.it().getVkToken());
        gson = new Gson();
    }

    public GroupFull getGroupInfo(int id) throws ClientException, ApiException {
        final GroupsGetByIdQuery query = vk.groups().getById(userActor);
        query.groupId(String.valueOf(id));
        final List<GroupFull> fullList = query.execute();
        return fullList.get(0);
    }

    public ExtendedInfo getPostsExt() throws ClientException, ApiException {
        final ExtendedInfo result = new ExtendedInfo();
        int offset = 0;
        while (true) {
            final WallGetQueryWithExtended wallGetQuery = vk.wall().getExtended(userActor);
            wallGetQuery.ownerId(-Settings.it().getVkGroupId());
            wallGetQuery.count(LIMIT);
            wallGetQuery.offset(offset);
            wallGetQuery.unsafeParam("fields", "first_name, last_name");

            final GetExtendedResponse response = wallGetQuery.execute();
            List<UserFull> profiles = response.getProfiles();
            if(response.getItems().isEmpty())
                break;
            result.addPosts(response.getItems());
            result.addProfiles(response.getProfiles());
            offset += 100;
            doWait();
        }

        logger.info("Collected " + result.size() + " posts.");
        return result;
    }


//    public Pair<List<PostItem>, List<Profile>> getPosts() throws ClientException {
//           int expectedCount = 0;
//           final Pair<List<PostItem>, List<Profile>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());
//           int offset = 0;
//           while (true) {
//               final WallGetQuery wallGetQuery = vk.wall().get(userActor);
//               wallGetQuery.ownerId(-Settings.VK_GROUP_ID);
//               wallGetQuery.count(LIMIT);
//               wallGetQuery.offset(offset);
//               wallGetQuery.unsafeParam("extended", 1);
//               wallGetQuery.unsafeParam("fields", "first_name, last_name");
//
//               final String json = wallGetQuery.executeAsString();
//               final Response response = gson.fromJson(json, Response.class);
//               if (response.getItems().isEmpty()) {
//                   expectedCount = response.getCount();
//                   break;
//               }
//               result.getKey().addAll(response.getItems());
//               result.getValue().addAll(response.getProfiles());
//               offset += 100;
//               doWait();
//           }
//           if (expectedCount != result.getKey().size())
//               logger.warn("Expected " + expectedCount + " posts. But collected " + result.getKey().size());
//           else
//               logger.info("Collected " + expectedCount + " posts.");
//           return result;
//       }


    private void doWait() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }


}
