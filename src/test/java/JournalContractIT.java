import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.Lists;
import org.web3j.model.Journal;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

@Testcontainers
public class JournalContractIT {

    @Container
    private static final GenericContainer ganacheContainer =
            new GenericContainer<>("trufflesuite/ganache-cli:latest")
                    .withExposedPorts(8545)
                    .withCreateContainerCmdModifier(cmd ->
                            cmd.withCmd("--gasLimit=0x1fffffffffffff", "--allowUnlimitedContractSize", "-e", "1000000000"));

    private Journal contract;
    private String secondaryAccount;
    private Web3j web3j;

    @BeforeEach
    void setup() throws Exception {
        HttpService web3jService = new HttpService(
                "http://" + ganacheContainer.getHost() + ":" + ganacheContainer.getMappedPort(8545));
        web3j = Web3j.build(web3jService);
        ContractGasProvider contractGasProvider = new DefaultGasProvider();
        Admin admin = Admin.build(web3jService);
        List<String> accounts = admin.personalListAccounts().send().getAccountIds();
        ClientTransactionManager transactionManager = new ClientTransactionManager(web3j, accounts.get(0));
        contract = Journal.deploy(web3j, transactionManager, contractGasProvider).send();
        secondaryAccount = accounts.get(1);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testJournal() throws Exception {
        String txHash = contract.insertNote("some_title", "some content..", Collections.singletonList(secondaryAccount), Arrays.asList("#new", "#test")).send().getTransactionHash();
        TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
        List<Journal.NoteInsertedEventResponse> notes = contract.getNoteInsertedEvents(receipt);
        notes.stream().flatMap(x -> x.taggedMembers.stream())
                .forEach(x -> assertTrue(x instanceof String) );
        notes.stream().flatMap(x -> x.tags.stream())
                .forEach(x -> assertTrue(x instanceof String) );
    }

}
