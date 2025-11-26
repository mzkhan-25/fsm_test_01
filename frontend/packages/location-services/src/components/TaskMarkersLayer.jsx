import TaskMarker from './TaskMarker';
import './TaskMarker.css';

/**
 * TaskMarkersLayer component that renders multiple task markers on the map
 * @param {Object} props - Component props
 * @param {Array} props.tasks - Array of task objects with coordinates
 * @param {function} props.onTaskClick - Optional handler called when a task marker is clicked
 */
const TaskMarkersLayer = ({ tasks = [], onTaskClick }) => {
  if (!Array.isArray(tasks) || tasks.length === 0) {
    return null;
  }

  return (
    <>
      {tasks.map((task) => (
        <TaskMarker
          key={task.id}
          task={task}
          onClick={onTaskClick}
        />
      ))}
    </>
  );
};

export default TaskMarkersLayer;
