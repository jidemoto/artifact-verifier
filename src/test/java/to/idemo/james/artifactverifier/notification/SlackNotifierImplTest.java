package to.idemo.james.artifactverifier.notification;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlackNotifierImplTest {

    @Test
    public void testBindingWhenPresent() {
        SlackNotifierImpl.SlackNotifierConditional slackNotifierConditional = new SlackNotifierImpl.SlackNotifierConditional();
        ConditionContext conditionContext = mock(ConditionContext.class);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("notifications.slack.webhookUrl", "https://whatever.com");
        when(conditionContext.getEnvironment()).thenReturn(env);

        assertTrue(slackNotifierConditional.matches(conditionContext, null));
    }

    @Test
    public void testBindingWhenAbsent() {
        SlackNotifierImpl.SlackNotifierConditional slackNotifierConditional = new SlackNotifierImpl.SlackNotifierConditional();
        ConditionContext conditionContext = mock(ConditionContext.class);
        MockEnvironment env = new MockEnvironment();
        when(conditionContext.getEnvironment()).thenReturn(env);

        assertFalse(slackNotifierConditional.matches(conditionContext, null));
    }

}