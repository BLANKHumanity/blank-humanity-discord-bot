package com.blank.humanity.discordbot.smartcontracts;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>
 * Auto generated code.
 * <p>
 * <strong>Do not modify!</strong>
 * <p>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j
 * command line tools</a>, or the
 * org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen
 * module</a> to update.
 *
 * <p>
 * Generated with web3j version 1.4.1.
 */
@SuppressWarnings("rawtypes")
public class InitializerSmartContract extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_PROVENANCE = "PROVENANCE";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_FREEMINT = "freeMint";

    public static final String FUNC_FREELISTCLAIMED = "freelistClaimed";

    public static final String FUNC_FREELISTMERKLEROOT = "freelistMerkleRoot";

    public static final String FUNC_GETAPPROVED = "getApproved";

    public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OPENTOPUBLIC = "openToPublic";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_OWNEROF = "ownerOf";

    public static final String FUNC_PRICE = "price";

    public static final String FUNC_PUBLICMINT = "publicMint";

    public static final String FUNC_PUBLICSUPPLYAVAILABLE = "publicSupplyAvailable";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_RESERVEMINT = "reserveMint";

    public static final String FUNC_RESERVESUPPLYAVAILABLE = "reserveSupplyAvailable";

    public static final String FUNC_safeTransferFrom = "safeTransferFrom";

    public static final String FUNC_SALEISACTIVE = "saleIsActive";

    public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

    public static final String FUNC_SETBASEURI = "setBaseURI";

    public static final String FUNC_SETFREELISTMERKLEROOT = "setFreelistMerkleRoot";

    public static final String FUNC_SETOPENTOPUBLIC = "setOpenToPublic";

    public static final String FUNC_SETPRICE = "setPrice";

    public static final String FUNC_SETPROVENANCE = "setProvenance";

    public static final String FUNC_SETSALESTATE = "setSaleState";

    public static final String FUNC_SETWHITELISTMERKLEROOT = "setWhitelistMerkleRoot";

    public static final String FUNC_SETWHITELISTPHASE = "setWhitelistPhase";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOKENURI = "tokenURI";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_WHITELISTMERKLEROOT = "whitelistMerkleRoot";

    public static final String FUNC_WHITELISTMINT = "whitelistMint";

    public static final String FUNC_WHITELISTPHASECLAIMED = "whitelistPhaseClaimed";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event APPROVAL_EVENT = new Event("Approval",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    public static final Event APPROVALFORALL_EVENT = new Event("ApprovalForAll",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Bool>() {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event(
        "OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event TRANSFER_EVENT = new Event("Transfer",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    @Deprecated
    protected InitializerSmartContract(String contractAddress, Web3j web3j,
        Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected InitializerSmartContract(String contractAddress, Web3j web3j,
        Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected InitializerSmartContract(String contractAddress, Web3j web3j,
        TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice,
            gasLimit);
    }

    protected InitializerSmartContract(String contractAddress, Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager,
            contractGasProvider);
    }

    public List<ApprovalEventResponse> getApprovalEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(
            APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues
                .getIndexedValues()
                .get(0)
                .getValue();
            typedResponse.approved = (String) eventValues
                .getIndexedValues()
                .get(1)
                .getValue();
            typedResponse.tokenId = (BigInteger) eventValues
                .getIndexedValues()
                .get(2)
                .getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(
        EthFilter filter) {
        return web3j
            .ethLogFlowable(filter)
            .map(new Function<Log, ApprovalEventResponse>() {
                @Override
                public ApprovalEventResponse apply(Log log) {
                    Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(
                        APPROVAL_EVENT, log);
                    ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                    typedResponse.log = log;
                    typedResponse.owner = (String) eventValues
                        .getIndexedValues()
                        .get(0)
                        .getValue();
                    typedResponse.approved = (String) eventValues
                        .getIndexedValues()
                        .get(1)
                        .getValue();
                    typedResponse.tokenId = (BigInteger) eventValues
                        .getIndexedValues()
                        .get(2)
                        .getValue();
                    return typedResponse;
                }
            });
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock,
            getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public List<ApprovalForAllEventResponse> getApprovalForAllEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(
            APPROVALFORALL_EVENT, transactionReceipt);
        ArrayList<ApprovalForAllEventResponse> responses = new ArrayList<ApprovalForAllEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues
                .getIndexedValues()
                .get(0)
                .getValue();
            typedResponse.operator = (String) eventValues
                .getIndexedValues()
                .get(1)
                .getValue();
            typedResponse.approved = (Boolean) eventValues
                .getNonIndexedValues()
                .get(0)
                .getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(
        EthFilter filter) {
        return web3j
            .ethLogFlowable(filter)
            .map(new Function<Log, ApprovalForAllEventResponse>() {
                @Override
                public ApprovalForAllEventResponse apply(Log log) {
                    Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(
                        APPROVALFORALL_EVENT, log);
                    ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
                    typedResponse.log = log;
                    typedResponse.owner = (String) eventValues
                        .getIndexedValues()
                        .get(0)
                        .getValue();
                    typedResponse.operator = (String) eventValues
                        .getIndexedValues()
                        .get(1)
                        .getValue();
                    typedResponse.approved = (Boolean) eventValues
                        .getNonIndexedValues()
                        .get(0)
                        .getValue();
                    return typedResponse;
                }
            });
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock,
            getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
        return approvalForAllEventFlowable(filter);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(
            OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues
                .getIndexedValues()
                .get(0)
                .getValue();
            typedResponse.newOwner = (String) eventValues
                .getIndexedValues()
                .get(1)
                .getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        EthFilter filter) {
        return web3j
            .ethLogFlowable(filter)
            .map(new Function<Log, OwnershipTransferredEventResponse>() {
                @Override
                public OwnershipTransferredEventResponse apply(Log log) {
                    Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(
                        OWNERSHIPTRANSFERRED_EVENT, log);
                    OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                    typedResponse.log = log;
                    typedResponse.previousOwner = (String) eventValues
                        .getIndexedValues()
                        .get(0)
                        .getValue();
                    typedResponse.newOwner = (String) eventValues
                        .getIndexedValues()
                        .get(1)
                        .getValue();
                    return typedResponse;
                }
            });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock,
            getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public List<TransferEventResponse> getTransferEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(
            TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues
                .getIndexedValues()
                .get(0)
                .getValue();
            typedResponse.to = (String) eventValues
                .getIndexedValues()
                .get(1)
                .getValue();
            typedResponse.tokenId = (BigInteger) eventValues
                .getIndexedValues()
                .get(2)
                .getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(
        EthFilter filter) {
        return web3j
            .ethLogFlowable(filter)
            .map(new Function<Log, TransferEventResponse>() {
                @Override
                public TransferEventResponse apply(Log log) {
                    Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(
                        TRANSFER_EVENT, log);
                    TransferEventResponse typedResponse = new TransferEventResponse();
                    typedResponse.log = log;
                    typedResponse.from = (String) eventValues
                        .getIndexedValues()
                        .get(0)
                        .getValue();
                    typedResponse.to = (String) eventValues
                        .getIndexedValues()
                        .get(1)
                        .getValue();
                    typedResponse.tokenId = (BigInteger) eventValues
                        .getIndexedValues()
                        .get(2)
                        .getValue();
                    return typedResponse;
                }
            });
    }

    public Flowable<TransferEventResponse> transferEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock,
            getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<String> PROVENANCE() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_PROVENANCE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String to,
        BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_APPROVE,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String owner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_BALANCEOF,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, owner)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> freeMint(
        List<byte[]> merkleProof) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_FREEMINT,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils
                            .typeMap(merkleProof,
                                org.web3j.abi.datatypes.generated.Bytes32.class))),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> freelistClaimed(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_FREELISTCLAIMED,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<byte[]> freelistMerkleRoot() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_FREELISTMERKLEROOT,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
            }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<String> getApproved(BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_GETAPPROVED,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Boolean> isApprovedForAll(String owner,
        String operator) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_ISAPPROVEDFORALL,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, owner),
                    new org.web3j.abi.datatypes.Address(160, operator)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> name() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_NAME,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Boolean> openToPublic() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_OPENTOPUBLIC,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_OWNER,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ownerOf(BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_OWNEROF,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> price() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_PRICE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> publicMint() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_PUBLICMINT,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> publicSupplyAvailable() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_PUBLICSUPPLYAVAILABLE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint16>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_RENOUNCEOWNERSHIP,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> reserveMint(String to) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_RESERVEMINT,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> reserveSupplyAvailable() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_RESERVESUPPLYAVAILABLE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint16>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from,
        String to, BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_safeTransferFrom,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                    new org.web3j.abi.datatypes.Address(160, to),
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from,
        String to, BigInteger tokenId, byte[] _data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_safeTransferFrom,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                    new org.web3j.abi.datatypes.Address(160, to),
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId),
                    new org.web3j.abi.datatypes.DynamicBytes(_data)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> saleIsActive() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SALEISACTIVE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(
        String operator, Boolean approved) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETAPPROVALFORALL,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.Address(160, operator),
                    new org.web3j.abi.datatypes.Bool(approved)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setBaseURI(String baseURI) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETBASEURI,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Utf8String(baseURI)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setFreelistMerkleRoot(
        byte[] merkleRoot) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETFREELISTMERKLEROOT,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Bytes32(merkleRoot)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setOpenToPublic(
        Boolean isOpen) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETOPENTOPUBLIC,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Bool(isOpen)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPrice(BigInteger _price) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETPRICE,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Uint256(_price)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setProvenance(
        String provenance) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETPROVENANCE,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.Utf8String(provenance)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setSaleState(
        Boolean saleState) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETSALESTATE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Bool(saleState)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setWhitelistMerkleRoot(
        byte[] merkleRoot) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETWHITELISTMERKLEROOT,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Bytes32(merkleRoot)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setWhitelistPhase(
        BigInteger _whitelistPhase) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SETWHITELISTPHASE,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(
                    _whitelistPhase)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SUPPORTSINTERFACE,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_SYMBOL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> tokenURI(BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_TOKENURI,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_TOTALSUPPLY,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from,
        String to, BigInteger tokenId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_TRANSFERFROM,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                    new org.web3j.abi.datatypes.Address(160, to),
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(
        String newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_TRANSFEROWNERSHIP,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> whitelistMerkleRoot() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_WHITELISTMERKLEROOT,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
            }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> whitelistMint(
        List<byte[]> merkleProof) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_WHITELISTMINT,
            Arrays
                .<Type>asList(
                    new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils
                            .typeMap(merkleProof,
                                org.web3j.abi.datatypes.generated.Bytes32.class))),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> whitelistPhaseClaimed(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_WHITELISTPHASECLAIMED,
            Arrays
                .<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
            FUNC_WITHDRAW,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static InitializerSmartContract load(String contractAddress,
        Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new InitializerSmartContract(contractAddress, web3j, credentials,
            gasPrice, gasLimit);
    }

    @Deprecated
    public static InitializerSmartContract load(String contractAddress,
        Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new InitializerSmartContract(contractAddress, web3j,
            transactionManager, gasPrice, gasLimit);
    }

    public static InitializerSmartContract load(String contractAddress,
        Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new InitializerSmartContract(contractAddress, web3j, credentials,
            contractGasProvider);
    }

    public static InitializerSmartContract load(String contractAddress,
        Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new InitializerSmartContract(contractAddress, web3j,
            transactionManager, contractGasProvider);
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String approved;

        public BigInteger tokenId;
    }

    public static class ApprovalForAllEventResponse extends BaseEventResponse {
        public String owner;

        public String operator;

        public Boolean approved;
    }

    public static class OwnershipTransferredEventResponse
        extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger tokenId;
    }
}
