package pl.quider.standalone.irc;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import pl.quider.standalone.irc.dbsession.ADatabaseSession;
import pl.quider.standalone.irc.dbsession.MySqlDatabaseSession;
import pl.quider.standalone.irc.model.Message;
import pl.quider.standalone.irc.services.ChannelService;
import pl.quider.standalone.irc.services.MessageService;
import pl.quider.standalone.irc.services.UserService;
import pl.quider.standalone.irc.verbs.Op;
import pl.quider.standalone.irc.verbs.Verb;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

/**
 * Created by Adrian on 27.09.2016.
 */
public class MyBot extends PircBot {

    public static final String VERB_PARAM_DELIMITER = "&%%&";
    boolean isOp;
    private Session session;
    private ADatabaseSession dbSession;
    private Verb verb;


    public MyBot(ADatabaseSession session) {
        super();
        this.dbSession = session;
        this.session = session.getSession();

    }

    @Override
    public void setAutoNickChange(boolean autoNickChange) {
        super.setAutoNickChange(autoNickChange);
    }

    @Override
    protected void onConnect() {
        super.onConnect();
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
    }

    @Override
    protected void onServerResponse(int code, String response) {
        switch (code) {
            case 311:
                if (this.verb != null) {
                    this.verb.execute(response);
                    this.verb = null;
                }
                break;
        }
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        for (User usr : users) {
            String nick = usr.getNick();
            if(usr.isOp()) {
                this.opNick(nick);
            }
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        UserService userService = new UserService(sender, login, hostname, session);
        pl.quider.standalone.irc.model.User user = userService.getUser();
        userService.userPresent(user);
        userService.updateStats(message);

        Message msg = new Message(channel, sender, user, message);
        MessageService messageService = new MessageService(msg, this, this.session);
        messageService.executeVerb(this);

        ChannelService channelService = new ChannelService(this.session);
        channelService.updateStats(user, msg);
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        UserService userService = new UserService(sender, login, hostname, this.session);
        userService.joined(channel);
        pl.quider.standalone.irc.model.User user = userService.getUser();
        if (user.isOp()) {
            this.op(channel, user.getNickName());
        }
    }

    @Override
    protected void onTopic(String channel, String topic) {
        super.onTopic(channel, topic);
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        super.onTopic(channel, topic, setBy, date, changed);
    }

    @Override
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        this.opNick(recipient);
    }

    /**
     * Sends WHOIS and after response finds a user in
     * database and op it
     * @param recipient
     */
    private void opNick(String recipient) {
        this.verb = new Op(this, null);
        sendRawLine("WHOIS " + recipient);
    }

    @Override
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        super.onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    @Override
    public String getNick() {
        return super.getNick();
    }

    @Override
    public int[] longToIp(long address) {
        return super.longToIp(address);
    }

    @Override
    public long ipToLong(byte[] address) {
        return super.ipToLong(address);
    }

    @Override
    public void setEncoding(String charset) throws UnsupportedEncodingException {
        super.setEncoding(charset);
    }

    @Override
    public String getEncoding() {
        return super.getEncoding();
    }

    @Override
    public InetAddress getInetAddress() {
        return super.getInetAddress();
    }

    @Override
    public void setDccInetAddress(InetAddress dccInetAddress) {
        super.setDccInetAddress(dccInetAddress);
    }

    @Override
    public InetAddress getDccInetAddress() {
        return super.getDccInetAddress();
    }

    @Override
    public int[] getDccPorts() {
        return super.getDccPorts();
    }

    @Override
    public void setDccPorts(int[] ports) {
        super.setDccPorts(ports);
    }

    public Session getSession() {
        return session;
    }
}
