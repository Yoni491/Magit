package ControlPackage;

import EngineRunner.ModuleTwo;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Commit.Commit;
import Objects.Commit.CommitCannotExecutException;
import Repository.DeleteHeadBranchException;
import Repository.NoActiveRepositoryException;
import Repository.NoSuchBranchException;
import Repository.NoSuchRepoException;
import XML.XmlNotValidException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class Controller {
    private boolean commitBool = false;
    @FXML
    private Label repositoryNameLabel;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label activeBranchLabel;

    @FXML
    private Label optionsLabel1;

    @FXML
    private Button switchButton1;

    @FXML
    private Button switchButton2;

    @FXML
    void showChanges(ActionEvent event) {
            JOptionPane.showMessageDialog(null, ModuleTwo.showStatus(), "Changes in repository", JOptionPane.INFORMATION_MESSAGE);
    }
    @FXML
    void commitButton() {
        TextInputDialog commitDialog = new TextInputDialog("");
        commitDialog.setTitle("Execute commit");
        commitDialog.setHeaderText("Enter commit message:");
        Optional<String> commitMsg = commitDialog.showAndWait();
        try {
            if (commitMsg.isPresent()) {
                ModuleTwo.executeCommit(commitMsg.get());
                buildFileTree(ModuleTwo.getActiveRepoPath());
                buildBranchCommitTree();
                activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
            }
        } catch (NoActiveRepositoryException | CommitCannotExecutException | AlreadyExistingBranchException e) {
            popAlert(e);
        }
    }
    @FXML
    void switchingButton1(ActionEvent event) {
        String branchName;
        if(commitBool) {
            TreeItem<CommitOrBranch> selectedItem = BranchCommitTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem.getValue().isCommit()) {
                Commit selectedCommit = selectedItem.getValue().getCommit();
                commitMsgLabel.setText(selectedCommit.getCommitPurposeMSG());
                showCommitFiles(selectedCommit);
            } else
                commitMsgLabel.setText("This is not a Commit");
        }
        else
            try {
                branchName=BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().getBranch().getName();
                ModuleTwo.checkout(branchName);
                activeBranchLabel.setText(branchName);
                buildBranchCommitTree();
                refreshFilesTree();
            } catch (NoActiveRepositoryException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
    }

    @FXML
    void switchingButton2(ActionEvent event) {
        if (commitBool) {
            Commit selectedCommit = BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().getCommit();
            try {
                ModuleTwo.resetActiveRepoHeadBranch(selectedCommit);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshCommitsTree(event);
        } else {
            try {
                ModuleTwo.deleteBranch(BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().branch.getName());
                buildBranchCommitTree();
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }
    @FXML
    private Label fileNameLabel;

    @FXML
    private ListView<?> fileContentListView;

    @FXML
    private TreeView<File> fileSystemTreeView;

    @FXML
    void refreshCommitsTree(ActionEvent event) {
        buildBranchCommitTree();
    }


    @FXML
    void refreshGraphic(MouseEvent event) {
    }
    private void switchCommitBranchesButtons() {//amos help with this exceptions<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        if(BranchCommitTreeView.getSelectionModel().getSelectedItem()!=null) {
            if (BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().isCommit()) {
                optionsLabel1.setText("Commit options:");
                switchButton1.setText("Show commit");
                switchButton2.setText("Reset head branch to this commit");
                commitBool = true;
            } else {
                optionsLabel1.setText("Branches options:");
                switchButton1.setText("Checkout");
                switchButton2.setText("Delete branch");

                commitBool = false;
            }
        }

    }

    private void showCommitFiles(Commit selectedCommit) {
        String path = ModuleTwo.getActiveRepoPath() + "/.magit/Commit files";
        try {
            ModuleTwo.getActiveRepo().deleteWCfiles(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        makeFilesOfCommit(selectedCommit, path);
        buildFileTree(path);
    }

    private void makeFilesOfCommit(Commit selectedCommit, String _path) {
        ModuleTwo.getActiveRepo().deployCommit(selectedCommit, _path);
    }

    @FXML
    void showContentButton() {
        TreeItem<File> selectedItem = fileSystemTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fileNameLabel.setText(selectedItem.getValue().getName());
            try {
                ObservableList list = FXCollections.observableArrayList(FileUtils.readLines(selectedItem.getValue(), "utf-8"));
                fileContentListView.setItems(list);
            } catch (IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void refreshFilesTree() {
        buildFileTree(ModuleTwo.getActiveRepoPath());
    }

    @FXML
    private Label commitMsgLabel;

    @FXML
    private TreeView<CommitOrBranch> BranchCommitTreeView;


    @FXML
    void createEmptyRepo() {
        String path;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File directory = directoryChooser.showDialog(new Stage());
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            path = directory.getPath() + "/" + answer.get();
            try {
                ModuleTwo.InitializeRepo(path);
                repositoryNameLabel.setText(answer.get());
            } catch (IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void deleteExistingBranch() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Delete branch");
        dialog.setHeaderText("Please enter the branch name to delete it:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.deleteBranch(answer.get());
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void makeNewBranch() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Make new branch");
        dialog.setHeaderText("Please enter the branch name:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.makeNewBranch(answer.get());

                String[] options = new String[]{"Yes",
                        "No"};
                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(options[0], options);
                choiceDialog.setTitle("Change active branch");
                choiceDialog.setHeaderText("Do you want to make the new branch active?");
                choiceDialog.setContentText("Please choose an option");
                Optional<String> answer2 = choiceDialog.showAndWait();
                if (answer2.isPresent()) {
                    if (answer2.get().equals(options[0])) {
                        ModuleTwo.checkout(answer.get());
                        activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
                    }
                }
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);

            } catch (NoActiveRepositoryException | AlreadyExistingBranchException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void loadRepoFromXml(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select XML file");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XML Document", "*.xml");
        fileChooser.getExtensionFilters().addAll(extensionFilter);
        File file = fileChooser.showOpenDialog(new Stage());
        try {
            ModuleTwo.loadRepo(file.getPath());
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);

        } catch (NoSuchRepoException | XmlNotValidException | IOException e) {
            popAlert(e);
        }
    }

    private TreeItem<File> getNodesForDirectory(File directory) {
        TreeItem<File> root = new TreeItem<>(directory);
        for (File f : Objects.requireNonNull(directory.listFiles())) {
            if (f.isDirectory() && !f.getName().equals(".magit"))
                root.getChildren().add(getNodesForDirectory(f));
            else if (!f.isDirectory())
                root.getChildren().add(new TreeItem<>(f));
        }
        return root;
    }

    private TreeItem<CommitOrBranch> getNodesForBranch() {
        TreeItem<CommitOrBranch> root = new TreeItem<>();
        List<Commit> commitLst;
        TreeItem<CommitOrBranch> headNode =new TreeItem<>(new CommitOrBranch(ModuleTwo.getActiveRepo().getHeadBranch()));
        commitLst = ModuleTwo.getActiveReposBranchCommits(ModuleTwo.getActiveRepo().getHeadBranch());
        headNode.getChildren().addAll(commitLst.stream().map(c -> new TreeItem<>(new CommitOrBranch(c))).collect(Collectors.toList()));
        root.getChildren().add(headNode);
        for (Branch b : ModuleTwo.getActiveReposBranches()) {
            TreeItem<CommitOrBranch> node = new TreeItem<>(new CommitOrBranch(b));
            commitLst = ModuleTwo.getActiveReposBranchCommits(b);
            node.getChildren().addAll(commitLst.stream().map(c -> new TreeItem<>(new CommitOrBranch(c))).collect(Collectors.toList()));
            root.getChildren().add(node);
        }
        BranchCommitTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> switchCommitBranchesButtons());
        return root;
    }


    private void buildFileTree(String activeRepoName) {
        fileSystemTreeView.setRoot(getNodesForDirectory(new File(activeRepoName)));
        fileSystemTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

            public TreeCell<File> call(TreeView<File> tv) {
                return new TreeCell<File>() {

                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : item.getName());
                    }

                };
            }
        });
    }



    private void buildBranchCommitTree() {
        BranchCommitTreeView.setRoot(getNodesForBranch());
        BranchCommitTreeView.setCellFactory(new Callback<TreeView<CommitOrBranch>, TreeCell<CommitOrBranch>>() {

            public TreeCell<CommitOrBranch> call(TreeView<CommitOrBranch> tv) {
                return new TreeCell<CommitOrBranch>() {

                    @Override
                    protected void updateItem(CommitOrBranch item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : (item.isCommit()) ? "Commit " + item.getCommit().getSha1() : "Branch " + item.getBranch().getName());
                    }

                };
            }
        });
        BranchCommitTreeView.getRoot().setExpanded(true);
    }


    @FXML
    void openRepository(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please choose the directory");
        File dir = directoryChooser.showDialog(new Stage());
        try {
            ModuleTwo.SwitchRepo(dir.getPath());
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
        } catch (NoSuchRepoException e) {
            popAlert(e);
        }
    }


    @FXML
    void showBranches(ActionEvent event) {
        String branches = "";
        branches = branches.concat("HEAD: "+ ModuleTwo.getActiveBranchName());
        for (Branch b : ModuleTwo.getActiveReposBranches()) {
            branches = branches.concat("\n" + b.getName());
        }
        JOptionPane.showMessageDialog(null, branches, "Active Repository Branches", JOptionPane.INFORMATION_MESSAGE);
    }

    @FXML
    void switchHeadBranch(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Please enter the branch name to switch to:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.checkout(answer.get());
                activeBranchLabel.setText(answer.get());
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
                refreshFilesTree();
            } catch (NoActiveRepositoryException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void switchUsername(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Switch username");
        dialog.setHeaderText("Please enter username:");
        dialog.setContentText("username");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            ModuleTwo.updateUsername(answer.get());
            usernameLabel.setText(answer.get());
        }
    }

    private void popAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }


    public static boolean deleteOrNot() {
        String[] options = new String[]{"Delete the repository to make a new one",
                "Keep existing repository and not load from XML."};
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(options[0], options);
        choiceDialog.setTitle("Magit repository already exists");
        choiceDialog.setHeaderText("There is an existing magit repository in location.");
        choiceDialog.setContentText("Please choose an option");
        Optional<String> answer = choiceDialog.showAndWait();
        return answer.map(s -> s.equals(options[0])).orElse(true);
    }

    private class CommitOrBranch {


        Branch branch;
        Commit commit;

        CommitOrBranch(Branch b) {
            branch = b;
        }

        CommitOrBranch(Commit c) {
            commit = c;
        }

        boolean isCommit() {
            return commit != null;
        }

        public Branch getBranch() {
            return branch;
        }

        public Commit getCommit() {
            return commit;
        }

    }
}

