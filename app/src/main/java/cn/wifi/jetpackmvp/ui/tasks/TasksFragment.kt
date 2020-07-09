package cn.wifi.jetpackmvp.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.wifi.jetpackmvp.R
import cn.wifi.jetpackmvp.data.model.Task
import cn.wifi.jetpackmvp.util.showSnackBar
import cn.wifi.jetpackmvp.view.ScrollChildSwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class TasksFragment : Fragment(), TasksContract.View {

    override lateinit var presenter: TasksContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    private lateinit var noTasksView: View
    private lateinit var noTaskIcon: ImageView
    private lateinit var noTaskMainView: TextView
    private lateinit var noTaskAddView: TextView
    private lateinit var tasksView: LinearLayout
    private lateinit var filteringLabelView: TextView

    /**
     * Listener for clicks on tasks in the ListView.
     */
    internal var itemListener: TaskItemListener = object : TaskItemListener {
        override fun onTaskClick(clickedTask: Task) {
            presenter.openTaskDetails(clickedTask)
        }

        override fun onCompleteTaskClick(completedTask: Task) {
            presenter.completeTask(completedTask)
        }

        override fun onActivateTaskClick(activatedTask: Task) {
            presenter.activateTask(activatedTask)
        }
    }

    private val listAdapter = TasksAdapter(ArrayList(0), itemListener)

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.result(requestCode, resultCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tasks_frag, container, false)

        with(root) {
            val listView = findViewById<ListView>(R.id.refresh_layout).apply { adapter = listAdapter }

            findViewById<ScrollChildSwipeRefreshLayout>(R.id.refresh_layout).apply {
                setColorSchemeColors(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                    ContextCompat.getColor(requireContext(), R.color.colorAccent),
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                )
                scrollUpChild = listView
                setOnRefreshListener { presenter.loadTasks(false) }
            }

            filteringLabelView = findViewById(R.id.filtering_text)
            tasksView = findViewById(R.id.tasks_list)

            noTasksView = findViewById(R.id.no_tasks_layout)
            noTaskIcon = findViewById(R.id.no_tasks_icon)
            noTaskMainView = findViewById(R.id.no_tasks_text)
            noTaskAddView = (findViewById<TextView>(R.id.no_tasks_add)).also {
                it.setOnClickListener { showAddTask() }
            }
        }

        requireActivity().findViewById<FloatingActionButton>(R.id.add_task_fab).apply {
            setImageResource(R.drawable.ic_add)
            setOnClickListener { presenter.addNewTask() }
        }
        setHasOptionsMenu(true)

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> presenter.clearCompletedTasks()
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> presenter.loadTasks(true)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun setLoadingIndicator(active: Boolean) {
        val root = view ?: return
        with(root.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)) {
            post { isRefreshing = true }
        }
    }

    override fun showTasks(tasks: List<Task>) {
        listAdapter.tasks = tasks
        tasksView.visibility = View.VISIBLE
        noTasksView.visibility = View.GONE
    }

    override fun showAddTask() {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK)
    }

    override fun showTaskDetailsUi(taskId: String) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        val intent = Intent(context, TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        }
        startActivity(intent)
    }

    override fun showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete))
    }

    override fun showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active))
    }

    override fun showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared))
    }

    override fun showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error))
    }

    override fun showNoTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_all), R.drawable.ic_assignment_turned_in_24dp, false)
    }

    override fun showActiveFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_active)
    }

    override fun showCompletedFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_completed)
    }

    override fun showAllFilterLabel() {
        filteringLabelView.text = resources.getString(R.string.label_all)
    }

    override fun showNoActiveTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_active), R.drawable.ic_check_circle_96dp, false)
    }

    override fun showNoCompletedTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_completed), R.drawable.ic_verified_user_96dp, false)
    }

    override fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message))
    }

    override fun showFilteringPopUpMenu() {
        val activity = activity ?: return
        val context = context ?: return
        PopupMenu(context, activity.findViewById(R.id.menu_filter)).apply {
            menuInflater.inflate(R.menu.filter_tasks, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.active -> presenter.currentFiltering = TasksFilterType.ACTIVE_TASKS
                    R.id.completed -> presenter.currentFiltering = TasksFilterType.COMPLETED_TASKS
                    else -> presenter.currentFiltering = TasksFilterType.ALL_TASKS
                }
                presenter.loadTasks(false)
                true
            }
            show()
        }
    }

    private fun showMessage(message: String) {
        view?.showSnackBar(message, Snackbar.LENGTH_LONG)
    }

    private fun showNoTasksViews(mainText: String, iconRes: Int, showAddView: Boolean) {
        tasksView.visibility = View.GONE
        noTasksView.visibility = View.VISIBLE

        noTaskMainView.text = mainText
        noTaskIcon.setImageResource(iconRes)
        noTaskAddView.visibility = if (showAddView) View.VISIBLE else View.GONE
    }
}