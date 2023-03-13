package com.user.py.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.user.py.mapper.ChatRecordMapper;
import com.user.py.mapper.MessageMqMapper;
import com.user.py.mode.entity.MessageMq;
import com.user.py.mode.entity.Post;
import com.user.py.mode.entity.User;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.utils.ListUtil;
import com.user.py.utils.SensitiveUtils;
import com.user.py.utils.ThreadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


@SpringBootTest
class UserServiceTest {
    @Resource
    private IUserService userService;
    @Resource
    private MessageMqMapper messageMqMapper;
    @Resource
    private ChatRecordMapper chatRecordMapper;
    @Resource
    private IPostService postService;
    private static  final Snowflake snowflake = IdUtil.getSnowflake(1, 1);
    private static final List<String> txtTest = Arrays.asList("说到职场成功，许多人认为学历和能力是最重要的。但其实，对于年轻人来说，拥有良好的人脉关系也是至关重要的。在职场中，有一张广泛、稳定而又多元化的人脉关系网，可以帮助年轻人更快地了解行业内部消息、获取独家资源，从而提高个人发展速度，为自己的职业生涯打下坚实的基础。",
            "面对职场竞争，很多人都会陷入焦虑和困惑。因此，刷题、学技能成了他们不断追求的目标。然而，事实上，这仅仅只是成功的基础条件之一。如果想要在职场中真正做得出色，还需要具备沟通和协作能力、领导和管理能力等软实力。因此，对于年轻人来说，这些软实力的培养同样重要，甚至比硬实力更为关键",
            "在当今的数字时代，数据已经成为企业的核心资产。它们不仅可以提供给企业更准确、更全面的市场信息，还可以支持企业进行商业决策，改善产品设计和制造流程。因此，对于企业来说，科学合理地管理公司数据是非常重要的。只有这样，企业才能更好地应对市场变化，增强自身竞争力",
            "找工作是一个漫长而且曲折的过程，在这个过程中，你需要一定的耐心和毅力。同时，你也需要不断地更新自己的简历，根据所找工作的不同，适当调整和优化自己的简历，以符合雇主的需求。此外，还需要注意自己的形象和气质，给潜在雇主留下良好的第一印象",
            "近年来，随着智能家居和可穿戴设备的普及，人工智能也逐渐走进我们的日常生活。它已经开始影响着我们的生产、生活和社交方式。未来，基于人工智能的智能家居系统将更加普遍，与我们的日常生活密切相关。因此，掌握人工智能的相关知识和技能已经成为一项十分重要的技能",
            "全球化和跨国企业的崛起，使得国际商务成为了一个越来越重要的领域。无论是开展跨境电商、还是开拓海外市场，都需要了解各国家和地区的商业法规和文化背景。因此，学习国际商务知识和技能已经成为未来成功的必要条件之一",
            "行业竞争激烈，公司间的差异化竞争也越来越明显。因此，企业需要注重品牌建设，树立自己的品牌形象，提高自身的知名度和美誉度。品牌建设包括很多方面，例如品牌定位、品牌传播、品牌维护等，都需要企业进行科学规划和有效实施。",
            "近年来，随着互联网的普及，网络营销的重要性也日益凸显。通过各种在线渠道，企业可以更加精准地推广自己的产品和服务，提高销售和转化率。因此，对于企业来说，掌握网络营销的技能和方法已经成为不可或缺的竞争利器。",
            "与此同时，随着人们对健康的关注度越来越高，健康产业迅速发展起来。从保健品到健身器材，从医疗设备到医疗服务，健康产业已经覆盖了众多领域。因此，对于有志于进入健康产业的年轻人来说，掌握相关的知识和技能已经成为实现自我价值和创业成功的必要条件。",
            "在当今的社会中，创新已经成为了推动社会进步和经济发展的重要引擎。无论是在科技领域、文化领域还是商业领域，都需要有大量的创新和创造力。因此，对于年轻人来说，具备创新思维和创业精神已经成为了实现个人价值和职场成功的重要因素。",
            "随着经济环境的变化和市场需求的不断改变，很多传统行业都面临着巨大的挑战和压力。因此，企业需要不断调整自身的战略和模式，适应市场变化，才能在激烈的竞争中取得胜利。同时，企业也需要密切关注市场趋势和变化，及时调整自己的产品和服务，以满足消费者的需求。",
            "在当今数字时代，数据分析已经成为了一项十分重要的技能。通过分析数据，企业可以更好地了解自己的业务和市场情况，发现问题和机遇，优化自身的运营和管理流程。因此，对于有志于从事数据分析工作的年轻人来说，掌握数据分析的技能和方法已经成为非常重要的职业能力。",
            "在现代社会中，人口老龄化已经成为了一个普遍的趋势。因此，养老产业也迅速崛起，涵盖了众多子行业。无论是养老院、养老保险、还是养老服务等，都需要大量的专业人才来支持和推动其发展。因此，对于有志于进入养老产业的年轻人来说，掌握相关的知识和技能已经成为了非常重要的职业能力。",
            "如何有效地管理人力资本，已经成为企业成功的重要因素之一。在当今的职场中，员工不再只是简单的执行者，而是企业发展的重要组成部分。因此，通过制定科学的人力资源管理方案，企业可以更好地吸引、培养和留住优秀的人才，发挥员工的最大潜力，提升企业的综合竞争力。",
            "在时代的浪潮下，很多传统行业都在不断向数字化转型。因此，数字化转型已经成为了企业保持竞争优势和发展的必经之路。数字化转型包括很多方面，例如信息化、智能化、互联网化等，需要企业进行科学规划和有效实施。只有这样，企业才能更好地适应市场变化，赢得竞争优势。",
            "对于年轻人来说，创业已经成为了一条越来越受欢迎的道路。然而，在创业过程中，面临着各种风险和挑战。因此，对于想要创业的年轻人来说，需要具备敏锐的市场洞察力、良好的商业思维和灵活的应变能力。另外，创业者还需要注重团队建设和人际关系，以便更好地应对未来的挑战和机遇。",
            "在现代社会中，环保意识的提高已经成为一个全球性问题。无论是在政府、企业还是个人层面，都需要积极响应并采取行动来保护环境。因此，对于有志于从事环保事业的年轻人来说，掌握相关的知识和技能已经成为了非常重要的职业能力。同时，也需要注重与社会组织和政府部门的合作，以推动环保事业的不断发展。",
            "随着全球化和跨国企业的崛起，语言能力已经成为了在职场中非常重要的竞争优势。无论是在沟通交流还是商务谈判中，能够使用多种语言进行交流可以大大提高成功的概率。因此，对于有志于在国际职场中获得成功的年轻人来说，掌握流利的英语和其他外语已经成为必备的职业技能。",
            "在现代社会中，公共关系已经成为了企业成功的重要因素之一。企业需要通过有针对性的公关策略，树立良好的品牌形象，增强消费者和社会的信任和认可度。因此，对于有志于从事公关工作的年轻人来说，需要具备广泛的社交网络和卓越的沟通和协调能力，以实现良好的公关效果。",
            "在当今数字时代，虚拟现实和增强现实技术已经成为了热门的领域。通过这些技术，人们可以模拟出各种场景和体验，为用户带来更加真实、直观的感受。因此，对于有志于从事虚拟现实和增强现实相关工作的年轻人来说，掌握相关的知识和技能已经成为了非常重要的竞争优势。");
    private final List<String> url = Arrays.asList(
            "https://randomuser.me/api/portraits/men/1.jpg",
            "https://randomuser.me/api/portraits/women/2.jpg",
            "https://randomuser.me/api/portraits/men/3.jpg",
            "https://randomuser.me/api/portraits/women/4.jpg",
            "https://randomuser.me/api/portraits/men/5.jpg",
            "https://randomuser.me/api/portraits/women/6.jpg",
            "https://randomuser.me/api/portraits/men/7.jpg",
            "https://randomuser.me/api/portraits/women/8.jpg",
            "https://randomuser.me/api/portraits/men/9.jpg",
            "https://randomuser.me/api/portraits/women/10.jpg",
            "https://randomuser.me/api/portraits/men/11.jpg",
            "https://randomuser.me/api/portraits/women/12.jpg",
            "https://randomuser.me/api/portraits/men/13.jpg",
            "https://randomuser.me/api/portraits/women/14.jpg",
            "https://randomuser.me/api/portraits/men/15.jpg",
            "https://randomuser.me/api/portraits/women/16.jpg",
            "https://randomuser.me/api/portraits/men/17.jpg",
            "https://randomuser.me/api/portraits/women/18.jpg",
            "https://randomuser.me/api/portraits/men/19.jpg",
            "https://randomuser.me/api/portraits/women/20.jpg",
            "https://randomuser.me/api/portraits/men/21.jpg",
            "https://randomuser.me/api/portraits/women/22.jpg",
            "https://randomuser.me/api/portraits/men/23.jpg",
            "https://randomuser.me/api/portraits/women/24.jpg",
            "https://randomuser.me/api/portraits/men/25.jpg",
            "https://randomuser.me/api/portraits/women/26.jpg",
            "https://randomuser.me/api/portraits/men/27.jpg",
            "https://randomuser.me/api/portraits/women/28.jpg",
            "https://randomuser.me/api/portraits/men/29.jpg",
            "https://randomuser.me/api/portraits/women/30.jpg",
            "https://randomuser.me/api/portraits/men/31.jpg",
            "https://randomuser.me/api/portraits/women/32.jpg",
            "https://randomuser.me/api/portraits/men/33.jpg",
            "https://randomuser.me/api/portraits/women/34.jpg",
            "https://randomuser.me/api/portraits/men/35.jpg",
            "https://randomuser.me/api/portraits/women/36.jpg",
            "https://randomuser.me/api/portraits/men/37.jpg",
            "https://randomuser.me/api/portraits/women/38.jpg",
            "https://randomuser.me/api/portraits/men/39.jpg",
            "https://randomuser.me/api/portraits/women/40.jpg",
            "https://randomuser.me/api/portraits/men/41.jpg",
            "https://randomuser.me/api/portraits/women/42.jpg",
            "https://randomuser.me/api/portraits/men/43.jpg",
            "https://randomuser.me/api/portraits/women/44.jpg",
            "https://randomuser.me/api/portraits/men/45.jpg",
            "https://randomuser.me/api/portraits/women/46.jpg",
            "https://randomuser.me/api/portraits/men/47.jpg",
            "https://randomuser.me/api/portraits/women/48.jpg",
            "https://randomuser.me/api/portraits/men/49.jpg",
            "https://randomuser.me/api/portraits/women/50.jpg");

    @Test
    void getUserAvatarVoByIds() {
        List<String> collect = Arrays.asList("1", "2", "3");
        String ids = ListUtil.ListToString(collect);
        System.out.println(ids);


    }

    @Test
    void getMessageMq() {
        MessageMq messageMq = new MessageMq();
        messageMq.setMessageId("63f82f7f1eb0d7a9cd2b2cb5");
        messageMq.setMessageBody("12");
        System.out.println(messageMqMapper.insert(messageMq));
    }

    public static boolean T(AddCommentRequest addCommentRequest) throws Exception {
        String content = addCommentRequest.getContent();
        String sensitive = SensitiveUtils.sensitive(content);
        System.out.println(sensitive);
        addCommentRequest.setContent(sensitive);
        return true;
    }

    @Test
    public void updateReadBatchById() {
        //List<String> asList = Arrays.asList("1628657682522095617", "1628657728885932034");
        List<String> asList = new ArrayList<>();
        int i = chatRecordMapper.updateReadBatchById(asList);
        System.out.println(i);
    }

    @Test
    void installMysql() {
        Random random = new Random();
        int num = 200000 - 5000;
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            ArrayList<User> list = new ArrayList<>(num / 100);
            for (int j = 0; j < num / 100; j++) {
                User user = new User();
                String name = "用户" + snowflake.nextIdStr();
                user.setUsername(name);
                user.setUserAccount(name);
                int nextInt = random.nextInt(50);
                user.setAvatarUrl(url.get(nextInt));
                user.setGender("男");
                user.setPassword("12f1b52ae343c200f385276446a7d1e6");
                user.setTags("");
                user.setProfile("");
                user.setTel("120");
                user.setEmail("");
                user.setUserStatus(0);
                user.setRole(0);
                list.add(user);
            }
            CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
                userService.saveBatch(list);
            }, ThreadUtil.getThreadPool());
            futures.add(runAsync);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        System.out.println("==========================执行完毕！==============================");
    }

    @Test
    void getSearchUserByPageMap() {
        System.out.println("==========开始=======");
        long start = System.currentTimeMillis();
        Map<String, Object> map = userService.friendUserName(null, "测试数据", 2L, 12L);
        long end = System.currentTimeMillis();
        System.out.println("===================");
        System.out.println((end - start) / 1000 + " 秒");
    }

    public static void main(String[] args) {
        int j=0;
        for (int i = 0; i < 100; i++) {
            for (int z = 0; z < 1000000 / 100; z++) {
                j++;
                System.out.println(j);
            }
        }


    }

    @Test
    void InstallPost() {
        Random random = new Random();
        System.out.println("=======================开始========================");
        long start = System.currentTimeMillis();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        AtomicInteger c = new AtomicInteger();
        List<User> users = userService.list(wrapper);
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>(100);
        int j=0;
        for (int i = 0; i < 100; i++) {
            Set<Post> list = new HashSet<>(users.size()/100);
            for (int z = 0; z < users.size() / 100; z++) {
                Post post = new Post();
                post.setReviewStatus(1);
                post.setTags("1");
                String id = users.get(j).getId();
                post.setUserId(id);
                post.setContent(txtTest.get(random.nextInt(10)));
                post.setReviewMessage("");
                post.setViewNum(0);
                post.setThumbNum(0);
                list.add(post);
                j++;
            }
            CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
                boolean saveBatch = postService.saveBatch(list);
                System.out.println("执行结果 " + (c.incrementAndGet()) + "个 =====> " +saveBatch);
            }, ThreadUtil.getThreadPool());
            futures.add(runAsync);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        long end = System.currentTimeMillis();
        System.out.println("===========================执行完毕 时间: " + ((end - start) / 1000) + "秒=========================");
    }
}